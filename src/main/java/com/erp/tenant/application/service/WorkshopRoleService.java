package com.erp.tenant.application.service;

import com.erp.tenant.domain.exception.BranchNotFoundException;
import com.erp.tenant.domain.exception.ProfileNotFoundException;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.domain.model.PermissionCatalog;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAssignmentJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAuditLogJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAssignmentRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleAuditLogRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopRoleRepository;
import com.erp.tenant.infrastructure.security.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkshopRoleService {

    public record RoleTemplateView(
            String code,
            String name,
            String description,
            boolean activated,
            Set<String> permissions
    ) {}

    private record RoleTemplateDefinition(
            String code,
            String name,
            String description,
            Set<String> permissions
    ) {}

    private static final Map<String, RoleTemplateDefinition> ROLE_TEMPLATES = buildRoleTemplates();

    private static Map<String, RoleTemplateDefinition> buildRoleTemplates() {
        Map<String, RoleTemplateDefinition> templates = new LinkedHashMap<>();
        templates.put("BRANCH_MANAGER", new RoleTemplateDefinition(
                "BRANCH_MANAGER",
                "Branch Manager",
                "Manages daily operations, staff, inventory, quotes, and analytics for an assigned branch.",
                Set.of(
                        "WORKORDER_READ", "WORKORDER_EXECUTE",
                        "INVENTORY_READ", "INVENTORY_MANAGE",
                        "SERVICE_MANAGE",
                        "QUOTE_READ", "QUOTE_CREATE", "QUOTE_APPROVE", "QUOTE_CANCEL",
                        "VOUCHER_READ", "CHECKOUT_PROCESS",
                        "APPOINTMENT_MANAGE",
                        "STAFF_READ", "STAFF_MANAGE", "SPECIALTY_MANAGE",
                        "BRANCH_READ", "MEDIA_UPLOAD", "ANALYTICS_READ"
                )
        ));
        templates.put("SECRETARY", new RoleTemplateDefinition(
                "SECRETARY",
                "Secretary / Receptionist",
                "Handles front-desk appointments, read-only order/quote tracking, and checkout billing.",
                Set.of(
                        "APPOINTMENT_MANAGE",
                        "WORKORDER_READ",
                        "QUOTE_READ",
                        "VOUCHER_READ",
                        "CHECKOUT_PROCESS",
                        "BRANCH_READ",
                        "INVENTORY_READ"
                )
        ));
        return Map.copyOf(templates);
    }

    public record WorkshopRoleView(
            UUID id,
            UUID workshopId,
            String name,
            String description,
            boolean system,
            boolean active,
            Set<String> permissions
    ) {}

    public record WorkshopRoleAuditLogView(
            UUID id,
            UUID workshopId,
            UUID workshopRoleId,
            UUID actorUserId,
            UUID targetUserId,
            String actionType,
            String roleName,
            String permissionsSnapshot,
            Instant recordedAt
    ) {}

    private final SpringDataWorkshopRoleRepository roleRepository;
    private final SpringDataWorkshopRoleAssignmentRepository assignmentRepository;
    private final SpringDataWorkshopRoleAuditLogRepository auditLogRepository;

    public WorkshopRoleService(
            SpringDataWorkshopRoleRepository roleRepository,
            SpringDataWorkshopRoleAssignmentRepository assignmentRepository,
            SpringDataWorkshopRoleAuditLogRepository auditLogRepository) {
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkshopRoleView> listRoles(UUID workshopId) {
        return roleRepository.findByWorkshopIdAndActiveTrueOrderByNameAsc(workshopId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleTemplateView> listTemplates(UUID workshopId) {
        return ROLE_TEMPLATES.values().stream()
                .map(t -> new RoleTemplateView(
                        t.code(),
                        t.name(),
                        t.description(),
                        roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, t.code()),
                        t.permissions()
                ))
                .toList();
    }

    @Transactional
    public WorkshopRoleView activateTemplate(UUID workshopId, UUID actorUserId, String templateCode) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new IllegalArgumentException("Template code is required");
        }
        String normalizedCode = templateCode.trim().toUpperCase(Locale.ROOT);
        RoleTemplateDefinition def = ROLE_TEMPLATES.get(normalizedCode);
        if (def == null) {
            throw new IllegalArgumentException("Unknown role template: " + templateCode);
        }
        if (roleRepository.existsByWorkshopIdAndTemplateCodeIgnoreCase(workshopId, def.code())
                || roleRepository.existsByWorkshopIdAndNameIgnoreCase(workshopId, def.name())) {
            throw new IllegalStateException("Role template already activated in workshop: " + def.code());
        }
        Set<String> validated = validatePermissions(def.permissions());
        UUID tenantId = resolveTenantId(workshopId);
        Instant now = Instant.now();
        String serializedPerms = String.join(",", validated);
        WorkshopRoleJpaEntity entity = new WorkshopRoleJpaEntity(
                UUID.randomUUID(), tenantId, workshopId, def.name(), def.description(),
                false, true, serializedPerms, now, now
        );
        entity.setTemplateCode(def.code());
        roleRepository.save(entity);
        recordAudit(tenantId, workshopId, entity.getId(), actorUserId, null, "TEMPLATE_ACTIVATED", def.name(), serializedPerms);
        return toView(entity);
    }

    @Transactional
    public WorkshopRoleView createRole(UUID workshopId, UUID actorUserId, String name, String description, Set<String> permissions) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        Set<String> validated = validatePermissions(permissions);
        String trimmedName = name.trim();
        if (roleRepository.existsByWorkshopIdAndNameIgnoreCase(workshopId, trimmedName)) {
            throw new IllegalStateException("Role name already exists in workshop: " + trimmedName);
        }

        UUID tenantId = resolveTenantId(workshopId);
        Instant now = Instant.now();
        String serializedPerms = String.join(",", validated);
        WorkshopRoleJpaEntity entity = new WorkshopRoleJpaEntity(
                UUID.randomUUID(), tenantId, workshopId, trimmedName, description,
                false, true, serializedPerms, now, now
        );
        roleRepository.save(entity);
        recordAudit(tenantId, workshopId, entity.getId(), actorUserId, null, "CREATED", trimmedName, serializedPerms);
        return toView(entity);
    }

    @Transactional
    public WorkshopRoleView updateRole(UUID workshopId, UUID roleId, UUID actorUserId, String name, String description, Set<String> permissions) {
        WorkshopRoleJpaEntity entity = roleRepository.findByIdAndWorkshopId(roleId, workshopId)
                .orElseThrow(() -> new ProfileNotFoundException("Workshop role not found: " + roleId));
        if (entity.isSystem()) {
            throw new TenantAccessDeniedException("System roles are immutable");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        Set<String> validated = validatePermissions(permissions);
        String trimmedName = name.trim();
        if (!entity.getName().equalsIgnoreCase(trimmedName)
                && roleRepository.existsByWorkshopIdAndNameIgnoreCase(workshopId, trimmedName)) {
            throw new IllegalStateException("Role name already exists in workshop: " + trimmedName);
        }

        String serializedPerms = String.join(",", validated);
        entity.setName(trimmedName);
        entity.setDescription(description);
        entity.setPermissions(serializedPerms);
        entity.setUpdatedAt(Instant.now());
        roleRepository.save(entity);
        recordAudit(entity.getTenantId(), workshopId, entity.getId(), actorUserId, null, "UPDATED", trimmedName, serializedPerms);
        return toView(entity);
    }

    @Transactional
    public void deactivateRole(UUID workshopId, UUID roleId, UUID actorUserId) {
        WorkshopRoleJpaEntity entity = roleRepository.findByIdAndWorkshopId(roleId, workshopId)
                .orElseThrow(() -> new ProfileNotFoundException("Workshop role not found: " + roleId));
        if (entity.isSystem()) {
            throw new TenantAccessDeniedException("System roles cannot be deactivated");
        }
        entity.setActive(false);
        entity.setUpdatedAt(Instant.now());
        roleRepository.save(entity);
        recordAudit(entity.getTenantId(), workshopId, entity.getId(), actorUserId, null, "DEACTIVATED", entity.getName(), entity.getPermissions());
    }

    @Transactional
    public void assignRole(UUID workshopId, UUID roleId, UUID actorUserId, UUID targetUserId) {
        assignRole(workshopId, roleId, actorUserId, targetUserId, null);
    }

    @Transactional
    public void assignRole(UUID workshopId, UUID roleId, UUID actorUserId, UUID targetUserId, UUID branchId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("Target userId is required");
        }
        WorkshopRoleJpaEntity entity = roleRepository.findByIdAndWorkshopId(roleId, workshopId)
                .orElseThrow(() -> new ProfileNotFoundException("Workshop role not found: " + roleId));
        if (!entity.isActive()) {
            throw new IllegalArgumentException("Cannot assign an inactive workshop role");
        }
        boolean alreadyAssigned = branchId == null
                ? assignmentRepository.existsByWorkshopIdAndWorkshopRoleIdAndUserIdAndBranchIdIsNull(workshopId, roleId, targetUserId)
                : assignmentRepository.existsByWorkshopIdAndWorkshopRoleIdAndUserIdAndBranchId(workshopId, roleId, targetUserId, branchId);
        if (!alreadyAssigned) {
            assignmentRepository.save(new WorkshopRoleAssignmentJpaEntity(
                    UUID.randomUUID(), entity.getTenantId(), workshopId, roleId, targetUserId, branchId, Instant.now()
            ));
        }
        recordAudit(entity.getTenantId(), workshopId, roleId, actorUserId, targetUserId, "ASSIGNED", entity.getName(), entity.getPermissions());
    }

    @Transactional(readOnly = true)
    public List<WorkshopRoleAuditLogView> listAuditLogs(UUID workshopId) {
        return auditLogRepository.findByWorkshopIdOrderByRecordedAtDesc(workshopId).stream()
                .map(a -> new WorkshopRoleAuditLogView(
                        a.getId(), a.getWorkshopId(), a.getWorkshopRoleId(),
                        a.getActorUserId(), a.getTargetUserId(), a.getActionType(),
                        a.getRoleName(), a.getPermissionsSnapshot(), a.getRecordedAt()
                ))
                .toList();
    }

    private Set<String> validatePermissions(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("At least one permission is required");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String p : permissions) {
            if (p == null || p.isBlank()) {
                throw new IllegalArgumentException("Permission code cannot be blank");
            }
            String code = p.trim().toUpperCase();
            if ("PLATFORM_ADMIN".equals(code)) {
                throw new TenantAccessDeniedException("PLATFORM_ADMIN cannot be granted by workshop roles");
            }
            if (!PermissionCatalog.isValidAssignable(code)) {
                throw new IllegalArgumentException("Unknown permission code: " + code);
            }
            normalized.add(code);
        }
        return normalized;
    }

    private UUID resolveTenantId(UUID workshopId) {
        UUID ctx = TenantContextHolder.getTenantId();
        return ctx != null ? ctx : workshopId;
    }

    private void recordAudit(UUID tenantId, UUID workshopId, UUID roleId, UUID actorUserId, UUID targetUserId,
                             String actionType, String roleName, String permissionsSnapshot) {
        auditLogRepository.save(new WorkshopRoleAuditLogJpaEntity(
                UUID.randomUUID(), tenantId, workshopId, roleId,
                actorUserId != null ? actorUserId : workshopId,
                targetUserId, actionType, roleName, permissionsSnapshot, Instant.now()
        ));
    }

    private WorkshopRoleView toView(WorkshopRoleJpaEntity entity) {
        Set<String> perms = Arrays.stream(entity.getPermissions().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new WorkshopRoleView(
                entity.getId(), entity.getWorkshopId(), entity.getName(),
                entity.getDescription(), entity.isSystem(), entity.isActive(), perms
        );
    }
}
