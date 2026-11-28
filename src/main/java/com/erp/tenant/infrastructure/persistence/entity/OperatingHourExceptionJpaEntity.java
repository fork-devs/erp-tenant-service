package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "operating_hour_exceptions")
public class OperatingHourExceptionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "scope_type", nullable = false, length = 20)
    private String scopeType;

    @Column(name = "scope_id", nullable = false)
    private UUID scopeId;

    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @Column(nullable = false)
    private boolean closed;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OperatingHourExceptionJpaEntity() {}

    public OperatingHourExceptionJpaEntity(UUID id, UUID tenantId, String scopeType, UUID scopeId,
                                           LocalDate exceptionDate, boolean closed, LocalTime openTime,
                                           LocalTime closeTime, String reason, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.exceptionDate = exceptionDate;
        this.closed = closed;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getScopeType() { return scopeType; }
    public UUID getScopeId() { return scopeId; }
    public LocalDate getExceptionDate() { return exceptionDate; }
    public boolean isClosed() { return closed; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public String getReason() { return reason; }
}
