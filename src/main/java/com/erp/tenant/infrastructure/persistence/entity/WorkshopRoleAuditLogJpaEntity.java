package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_role_audit_logs")
public class WorkshopRoleAuditLogJpaEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "workshop_role_id", nullable = false)
    private UUID workshopRoleId;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "permissions_snapshot", nullable = false, columnDefinition = "TEXT")
    private String permissionsSnapshot;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public WorkshopRoleAuditLogJpaEntity() {}

    public WorkshopRoleAuditLogJpaEntity(UUID id, UUID tenantId, UUID workshopId, UUID workshopRoleId,
                                         UUID actorUserId, UUID targetUserId, String actionType,
                                         String roleName, String permissionsSnapshot, Instant recordedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.workshopRoleId = workshopRoleId;
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.actionType = actionType;
        this.roleName = roleName;
        this.permissionsSnapshot = permissionsSnapshot;
        this.recordedAt = recordedAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getWorkshopId() { return workshopId; }
    public UUID getWorkshopRoleId() { return workshopRoleId; }
    public UUID getActorUserId() { return actorUserId; }
    public UUID getTargetUserId() { return targetUserId; }
    public String getActionType() { return actionType; }
    public String getRoleName() { return roleName; }
    public String getPermissionsSnapshot() { return permissionsSnapshot; }
    public Instant getRecordedAt() { return recordedAt; }
}
