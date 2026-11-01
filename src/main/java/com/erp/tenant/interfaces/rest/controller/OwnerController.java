package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.CreateOwnerProfileCommand;
import com.erp.tenant.application.dto.command.UpdateOwnerProfileCommand;
import com.erp.tenant.application.dto.result.OwnerProfileResult;
import com.erp.tenant.application.usecase.GetOwnerProfilesUseCase;
import com.erp.tenant.application.usecase.ManageOwnerProfileUseCase;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.infrastructure.security.MultiTenancySecurityService;
import com.erp.tenant.interfaces.rest.dto.request.CreateOwnerProfileRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateOwnerProfileRequest;
import com.erp.tenant.interfaces.rest.dto.response.OwnerProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Owner Profiles", description = "Endpoints para la gestión de perfiles de negocio de dueños de taller")
public class OwnerController {

    private final ManageOwnerProfileUseCase manageOwnerUseCase;
    private final GetOwnerProfilesUseCase getOwnerUseCase;
    private final MultiTenancySecurityService securityService;

    public OwnerController(ManageOwnerProfileUseCase manageOwnerUseCase,
                           GetOwnerProfilesUseCase getOwnerUseCase,
                           MultiTenancySecurityService securityService) {
        this.manageOwnerUseCase = manageOwnerUseCase;
        this.getOwnerUseCase = getOwnerUseCase;
        this.securityService = securityService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForSelf(#request.userId())")
    @Operation(summary = "Crear Registro de Dueño", description = "Crea perfil de dueño vinculado a un User (IAM)")
    public ResponseEntity<OwnerProfileResponse> createOwner(@Valid @RequestBody CreateOwnerProfileRequest request) {
        CreateOwnerProfileCommand command = new CreateOwnerProfileCommand(
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.documentType(),
                request.documentNumber(),
                request.phone()
        );
        OwnerProfileResult result = manageOwnerUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OwnerProfileResponse.from(result));
    }

    @GetMapping("/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener Dueño por ID", description = "Retorna perfil de dueño validando auto-gestión")
    public ResponseEntity<OwnerProfileResponse> getOwnerById(@PathVariable UUID ownerId) {
        OwnerProfileResult result = getOwnerUseCase.getById(ownerId);
        if (!securityService.isAuthorizedForSelf(result.userId())) {
            throw new TenantAccessDeniedException("Self-management access required to view owner profile: " + result.userId());
        }
        return ResponseEntity.ok(OwnerProfileResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener Dueño por User ID", description = "Busca owner profile por User ID (IAM)")
    public ResponseEntity<OwnerProfileResponse> getOwnerByUserId(@RequestParam(name = "userId") UUID userId) {
        if (!securityService.isAuthorizedForSelf(userId)) {
            throw new TenantAccessDeniedException("Self-management access required to view owner profile: " + userId);
        }
        OwnerProfileResult result = getOwnerUseCase.getByUserId(userId);
        return ResponseEntity.ok(OwnerProfileResponse.from(result));
    }

    @PutMapping("/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar Dueño", description = "Actualiza datos del perfil de dueño bajo auto-gestión")
    public ResponseEntity<OwnerProfileResponse> updateOwner(
            @PathVariable UUID ownerId,
            @RequestBody UpdateOwnerProfileRequest request) {
        OwnerProfileResult existing = getOwnerUseCase.getById(ownerId);
        if (!securityService.isAuthorizedForSelf(existing.userId())) {
            throw new TenantAccessDeniedException("Self-management access required to update owner profile: " + existing.userId());
        }

        UpdateOwnerProfileCommand command = new UpdateOwnerProfileCommand(
                ownerId,
                request.firstName(),
                request.lastName(),
                request.documentType(),
                request.documentNumber(),
                request.phone()
        );
        OwnerProfileResult updated = manageOwnerUseCase.update(command);
        return ResponseEntity.ok(OwnerProfileResponse.from(updated));
    }

    @DeleteMapping("/{ownerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar Dueño", description = "Elimina perfil de dueño (bloqueado si tiene talleres asociados)")
    public ResponseEntity<Void> deleteOwner(@PathVariable UUID ownerId) {
        OwnerProfileResult existing = getOwnerUseCase.getById(ownerId);
        if (!securityService.isAuthorizedForSelf(existing.userId())) {
            throw new TenantAccessDeniedException("Self-management access required to delete owner profile: " + existing.userId());
        }

        manageOwnerUseCase.delete(ownerId);
        return ResponseEntity.noContent().build();
    }
}
