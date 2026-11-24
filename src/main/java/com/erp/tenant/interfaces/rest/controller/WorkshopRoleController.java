package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.service.WorkshopRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workshops/{workshopId}/roles")
@Tag(name = "Workshop Roles", description = "Custom workshop role management, assignments, and audit logs")
public class WorkshopRoleController {

    public record UpsertWorkshopRoleRequest(
            @NotBlank String name,
            String description,
            @NotEmpty Set<String> permissions
    ) {}

    public record AssignWorkshopRoleRequest(@NotNull UUID userId) {}

    private final WorkshopRoleService workshopRoleService;

    public WorkshopRoleController(WorkshopRoleService workshopRoleService) {
        this.workshopRoleService = workshopRoleService;
    }

    @GetMapping
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "List workshop roles")
    public ResponseEntity<List<WorkshopRoleService.WorkshopRoleView>> listRoles(@PathVariable UUID workshopId) {
        return ResponseEntity.ok(workshopRoleService.listRoles(workshopId));
    }

    @PostMapping
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "Create custom workshop role")
    public ResponseEntity<WorkshopRoleService.WorkshopRoleView> createRole(
            @PathVariable UUID workshopId,
            @Valid @RequestBody UpsertWorkshopRoleRequest request,
            Authentication auth) {
        UUID actorId = resolveActorId(auth, workshopId);
        WorkshopRoleService.WorkshopRoleView created = workshopRoleService.createRole(
                workshopId, actorId, request.name(), request.description(), request.permissions()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "Update custom workshop role")
    public ResponseEntity<WorkshopRoleService.WorkshopRoleView> updateRole(
            @PathVariable UUID workshopId,
            @PathVariable UUID roleId,
            @Valid @RequestBody UpsertWorkshopRoleRequest request,
            Authentication auth) {
        UUID actorId = resolveActorId(auth, workshopId);
        return ResponseEntity.ok(workshopRoleService.updateRole(
                workshopId, roleId, actorId, request.name(), request.description(), request.permissions()
        ));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "Deactivate custom workshop role")
    public ResponseEntity<Void> deactivateRole(
            @PathVariable UUID workshopId,
            @PathVariable UUID roleId,
            Authentication auth) {
        workshopRoleService.deactivateRole(workshopId, roleId, resolveActorId(auth, workshopId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/assignments")
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "Assign custom role to user")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID workshopId,
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignWorkshopRoleRequest request,
            Authentication auth) {
        workshopRoleService.assignRole(workshopId, roleId, resolveActorId(auth, workshopId), request.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")
    @Operation(summary = "List workshop role audit logs")
    public ResponseEntity<List<WorkshopRoleService.WorkshopRoleAuditLogView>> listAuditLogs(@PathVariable UUID workshopId) {
        return ResponseEntity.ok(workshopRoleService.listAuditLogs(workshopId));
    }

    private UUID resolveActorId(Authentication auth, UUID fallback) {
        if (auth == null || auth.getName() == null) {
            return fallback;
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
