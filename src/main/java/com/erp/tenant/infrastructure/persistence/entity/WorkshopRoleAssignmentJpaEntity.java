package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_role_assignments")
public class WorkshopRoleAssignmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "workshop_role_id", nullable = false)
    private UUID workshopRoleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    public WorkshopRoleAssignmentJpaEntity() {}

    public WorkshopRoleAssignmentJpaEntity(UUID id, UUID tenantId, UUID workshopId, UUID workshopRoleId,
                                           UUID userId, Instant assignedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.workshopRoleId = workshopRoleId;
        this.userId = userId;
        this.assignedAt = assignedAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getWorkshopId() { return workshopId; }
    public UUID getWorkshopRoleId() { return workshopRoleId; }
    public UUID getUserId() { return userId; }
    public Instant getAssignedAt() { return assignedAt; }
}
