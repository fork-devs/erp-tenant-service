package com.erp.tenant.infrastructure.messaging.outbox;

import com.erp.tenant.application.usecase.ProcessOutboxMessagesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "erp.tenant.outbox.relay-enabled", havingValue = "true", matchIfMissing = true)
public class TenantOutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(TenantOutboxRelayScheduler.class);

    private final ProcessOutboxMessagesUseCase processOutboxMessagesUseCase;
    private final int batchSize;

    public TenantOutboxRelayScheduler(
            ProcessOutboxMessagesUseCase processOutboxMessagesUseCase,
            @Value("${erp.tenant.outbox.batch-size:50}") int batchSize) {
        this.processOutboxMessagesUseCase = processOutboxMessagesUseCase;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${erp.tenant.outbox.interval-ms:5000}")
    public void scheduleOutboxDispatch() {
        int processed = processOutboxMessagesUseCase.processUnpublishedMessages(batchSize);
        if (processed > 0) {
            log.debug("[OUTBOX-RELAY] Se procesaron y despacharon {} eventos de outbox", processed);
        }
    }
}
