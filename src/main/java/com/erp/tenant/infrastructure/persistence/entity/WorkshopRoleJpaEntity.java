package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_roles")
public class WorkshopRoleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public WorkshopRoleJpaEntity() {}

    public WorkshopRoleJpaEntity(UUID id, UUID tenantId, UUID workshopId, String name, String description,
                                 boolean system, boolean active, String permissions,
                                 Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.name = name;
        this.description = description;
        this.system = system;
        this.active = active;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getWorkshopId() { return workshopId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isSystem() { return system; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
