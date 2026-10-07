package com.erp.tenant.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TenantSuspendedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID tenantId;
    private final String ruc;
    private final String reason;
    private final Instant occurredAt;

    public TenantSuspendedEvent(UUID tenantId, String ruc, String reason) {
        this.eventId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.ruc = ruc;
        this.reason = reason;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "TenantSuspended";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getRuc() {
        return ruc;
    }

    public String getReason() {
        return reason;
    }
}
