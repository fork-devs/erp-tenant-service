package com.erp.tenant.application.service;

import com.erp.tenant.application.usecase.ProcessOutboxMessagesUseCase;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Application service that processes pending transactional outbox messages.
 *
 * <p>Ensures non-blocking error handling, exponential backoff retries,
 * and external publication outside the local database transaction boundary.
 */
public class OutboxProcessorService implements ProcessOutboxMessagesUseCase {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessorService.class);

    private final TenantOutboxRepositoryPort outboxRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final UnitOfWork unitOfWork;
    private final int maxAttempts;
    private final Duration maxDelay;

    public OutboxProcessorService(TenantOutboxRepositoryPort outboxRepositoryPort,
                                  EventPublisherPort eventPublisherPort,
                                  UnitOfWork unitOfWork,
                                  int maxAttempts,
                                  Duration maxDelay) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.unitOfWork = unitOfWork;
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
    }

    @Override
    public int processUnpublishedMessages(int batchSize) {
        // Message claiming runs in its own short transaction. Publication
        // occurs outside the database transaction boundary.
        List<TenantOutboxMessage> messages = unitOfWork.callInTransaction(
                () -> outboxRepositoryPort.findPending(Instant.now(), batchSize));

        int published = 0;
        for (TenantOutboxMessage message : messages) {
            try {
                eventPublisherPort.publish(message);
                message.markAsPublished();
                unitOfWork.runInTransaction(() -> outboxRepositoryPort.save(message));
                published++;
            } catch (Exception e) {
                // A failed message does not block the batch: it is recorded and
                // rescheduled, and processing of subsequent messages continues.
                log.warn("[OUTBOX-RELAY] Failed to publish outbox event {} ({}). Attempt {}/{}: {}",
                        message.getEventType(), message.getId(), message.getAttempts() + 1, maxAttempts, e.getMessage());
                message.recordFailure(e.getMessage(), maxAttempts, maxDelay);
                unitOfWork.runInTransaction(() -> outboxRepositoryPort.save(message));
            }
        }
        return published;
    }
}
