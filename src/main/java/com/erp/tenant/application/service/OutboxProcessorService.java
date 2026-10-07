package com.erp.tenant.application.service;

import com.erp.tenant.application.usecase.ProcessOutboxMessagesUseCase;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;

import java.util.List;

public class OutboxProcessorService implements ProcessOutboxMessagesUseCase {

    private final TenantOutboxRepositoryPort outboxRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public OutboxProcessorService(
            TenantOutboxRepositoryPort outboxRepositoryPort,
            EventPublisherPort eventPublisherPort) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public int processUnpublishedMessages(int batchSize) {
        List<TenantOutboxMessage> unpublishedMessages = outboxRepositoryPort.findUnpublished(batchSize);
        int processedCount = 0;

        for (TenantOutboxMessage message : unpublishedMessages) {
            try {
                eventPublisherPort.publish(message);
                message.markAsPublished();
                outboxRepositoryPort.save(message);
                processedCount++;
            } catch (Exception e) {
                // Se detiene o salta para reintentar en el siguiente ciclo
                break;
            }
        }

        return processedCount;
    }
}
