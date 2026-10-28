package com.erp.tenant.infrastructure.messaging.publisher;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogEventPublisherAdapter.class);

    @Override
    public void publish(TenantOutboxMessage message) {
        log.info("[OUTBOX-RELAY] Despachando evento de dominio '{}' para aggregateId='{}': {}",
                message.getEventType(), message.getAggregateId(), message.getPayload());
        // Connects to KafkaProducer when message broker is configured in later phases
    }
}
