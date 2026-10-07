package com.erp.tenant.domain.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Serializable {
    UUID getEventId();
    String getEventType();
    Instant getOccurredAt();
}
