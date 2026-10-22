package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TenantOutboxMessage implements Serializable {
    private final UUID id;
    private final String aggregateType;
    private final UUID aggregateId;
    private final UUID tenantId;
    private final String eventType;
    private final String payload;
    private boolean published;
    private final Instant createdAt;

    public TenantOutboxMessage(UUID id, String aggregateType, UUID aggregateId, UUID tenantId, String eventType, String payload, boolean published, Instant createdAt) {
        if (id == null || aggregateType == null || aggregateId == null || tenantId == null || eventType == null || payload == null) {
            throw new IllegalArgumentException("Campos obligatorios del OutboxMessage no pueden ser nulos");
        }
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.payload = payload;
        this.published = published;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static TenantOutboxMessage of(UUID aggregateId, UUID tenantId, String eventType, String payload) {
        return new TenantOutboxMessage(
                UUID.randomUUID(),
                "TENANT",
                aggregateId,
                tenantId,
                eventType,
                payload,
                false,
                Instant.now()
        );
    }

    public void markAsPublished() {
        this.published = true;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public boolean isPublished() {
        return published;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantOutboxMessage that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
