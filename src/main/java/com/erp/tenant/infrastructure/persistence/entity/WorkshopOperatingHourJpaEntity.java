package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "workshop_operating_hours")
public class WorkshopOperatingHourJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "day_of_week", nullable = false)
    private short dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public WorkshopOperatingHourJpaEntity() {}

    public WorkshopOperatingHourJpaEntity(UUID id, UUID tenantId, UUID workshopId, short dayOfWeek,
                                          LocalTime openTime, LocalTime closeTime, boolean active,
                                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getWorkshopId() { return workshopId; }
    public short getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public boolean isActive() { return active; }
}
