package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Mensaje pendiente de publicar en el bus de eventos.
 *
 * <p>El identificador del evento viaja en el mensaje y no se regenera en cada
 * reintento, de modo que un consumidor pueda deduplicar: la entrega es al
 * menos una vez, y el identificador es lo que hace la deduplicacion posible.
 */
public class TenantOutboxMessage implements Serializable {

    /**
     * Escala de esperas entre reintentos, en segundos. Es creciente y llega
     * hasta 24 horas, que es la ventana de recuperacion prevista para un
     * proveedor en modo degradado.
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
     * Registra un fallo de publicacion: incrementa el contador, guarda el
     * error y programa el proximo reintento con una espera creciente. Al
     * alcanzar el maximo de intentos, el mensaje pasa a cola de muertos y deja
     * de reintentarse solo, pero se conserva.
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
     * Devuelve el mensaje a la cola de pendientes, con el contador
     * restablecido, para que vuelva a intentarse.
     */
    public void reactivate() {
        this.attempts = 0;
        this.lastError = null;
        this.status = TenantOutboxStatus.PENDING;
        this.nextAttemptAt = Instant.now();
    }

    /**
     * Espera hasta el proximo intento. Es creciente con el numero de
     * intentos y nunca supera el maximo configurado.
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
