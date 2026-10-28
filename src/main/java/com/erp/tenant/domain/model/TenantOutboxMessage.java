package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Message pending publication on the event bus.
 *
 * <p>The event ID travels inside the message and is not regenerated across retries,
 * ensuring consumers can deduplicate: delivery is at-least-once, and the immutable event ID
 * enables deterministic idempotency.
 */
public class TenantOutboxMessage implements Serializable {

    /**
     * Exponential backoff scale between retry attempts in seconds. Ascending up to 24 hours,
     * matching the recovery window for degraded brokers.
     */
    private static final long[] BACKOFF_SECONDS = {5, 30, 300, 1800, 7200, 28800, 86400};

    private final UUID id;
    private final UUID eventId;
    private final String aggregateType;
    private final UUID aggregateId;
    private final UUID tenantId;
    private final String eventType;
    private final String payload;
    private int attempts;
    private Instant nextAttemptAt;
    private String lastError;
    private TenantOutboxStatus status;
    private final Instant createdAt;

    public TenantOutboxMessage(UUID id, UUID eventId, String aggregateType, UUID aggregateId, UUID tenantId,
                               String eventType, String payload, int attempts, Instant nextAttemptAt,
                               String lastError, TenantOutboxStatus status, Instant createdAt) {
        if (id == null || eventId == null || aggregateType == null || aggregateId == null || tenantId == null
                || eventType == null || payload == null || status == null) {
            throw new IllegalArgumentException("Campos obligatorios del OutboxMessage no pueden ser nulos");
        }
        this.id = id;
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.payload = payload;
        this.attempts = attempts;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static TenantOutboxMessage of(UUID aggregateId, UUID tenantId, UUID eventId, String eventType, String payload) {
        return new TenantOutboxMessage(
                UUID.randomUUID(),
                eventId,
                "TENANT",
                aggregateId,
                tenantId,
                eventType,
                payload,
                0,
                Instant.now(),
                null,
                TenantOutboxStatus.PENDING,
                Instant.now());
    }

    public void markAsPublished() {
        this.status = TenantOutboxStatus.PUBLISHED;
        this.lastError = null;
    }

    /**
     * Records a publication failure: increments attempt count, saves error details,
     * and schedules the next retry with exponential backoff. Upon reaching max attempts,
     * the message moves to the Dead Letter Queue (DLQ).
     */
    public void recordFailure(String error, int maxAttempts, Duration maxDelay) {
        this.attempts++;
        this.lastError = error;

        if (this.attempts >= maxAttempts) {
            this.status = TenantOutboxStatus.DEAD_LETTER;
            this.nextAttemptAt = null;
            return;
        }

        this.status = TenantOutboxStatus.PENDING;
        this.nextAttemptAt = Instant.now().plus(nextDelay(maxDelay));
    }

    /**
     * Returns the message to the pending queue with a reset attempt counter for manual retry.
     */
    public void reactivate() {
        this.attempts = 0;
        this.lastError = null;
        this.status = TenantOutboxStatus.PENDING;
        this.nextAttemptAt = Instant.now();
    }

    /**
     * Wait duration until the next attempt. Increases with retry count and never exceeds max backoff.
     */
    public Duration nextDelay(Duration maxDelay) {
        int index = Math.min(Math.max(attempts - 1, 0), BACKOFF_SECONDS.length - 1);
        Duration delay = Duration.ofSeconds(BACKOFF_SECONDS[index]);
        return delay.compareTo(maxDelay) > 0 ? maxDelay : delay;
    }

    public boolean isDeadLettered() {
        return status == TenantOutboxStatus.DEAD_LETTER;
    }

    public boolean isPublished() {
        return status == TenantOutboxStatus.PUBLISHED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
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

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public TenantOutboxStatus getStatus() {
        return status;
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
