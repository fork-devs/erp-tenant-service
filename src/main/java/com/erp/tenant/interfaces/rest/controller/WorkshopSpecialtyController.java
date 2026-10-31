package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.CreateSpecialtyCommand;
import com.erp.tenant.application.dto.command.UpdateSpecialtyCommand;
import com.erp.tenant.application.dto.result.SpecialtyResult;
import com.erp.tenant.application.dto.result.SpecialtyRevisionResult;
import com.erp.tenant.application.usecase.GetWorkshopSpecialtiesUseCase;
import com.erp.tenant.application.usecase.ManageWorkshopSpecialtyUseCase;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.domain.exception.TenantContextMissingException;
import com.erp.tenant.infrastructure.security.TenantContextHolder;
import com.erp.tenant.interfaces.rest.dto.request.CreateSpecialtyRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateSpecialtyRequest;
import com.erp.tenant.interfaces.rest.dto.response.SpecialtyResponse;
import com.erp.tenant.interfaces.rest.dto.response.SpecialtyRevisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workshops/{workshopId}/specialties")
@Tag(name = "Workshop Specialties", description = "Gestión del catálogo dinámico de especialidades de taller")
public class WorkshopSpecialtyController {

    private final ManageWorkshopSpecialtyUseCase manageSpecialtyUseCase;
    private final GetWorkshopSpecialtiesUseCase getSpecialtiesUseCase;

    public WorkshopSpecialtyController(ManageWorkshopSpecialtyUseCase manageSpecialtyUseCase,
                                       GetWorkshopSpecialtiesUseCase getSpecialtiesUseCase) {
        this.manageSpecialtyUseCase = manageSpecialtyUseCase;
        this.getSpecialtiesUseCase = getSpecialtiesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Crear Especialidad", description = "Agrega una especialidad técnica al catálogo del taller")
    public ResponseEntity<SpecialtyResponse> createSpecialty(
            @PathVariable UUID workshopId,
            @Valid @RequestBody CreateSpecialtyRequest request,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        CreateSpecialtyCommand command = new CreateSpecialtyCommand(
                workshopId,
                workshopId,
                request.name(),
                request.code(),
                request.description(),
                resolveActor(authentication)
        );

        SpecialtyResult result = manageSpecialtyUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(SpecialtyResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER', 'SERVICE_ADVISOR', 'ASESOR', 'TECHNICIAN', 'MECANICO')")
    @Operation(summary = "Listar Especialidades", description = "Lista las especialidades del taller, opcionalmente solo activas")
    public ResponseEntity<List<SpecialtyResponse>> listSpecialties(
            @PathVariable UUID workshopId,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        List<SpecialtyResponse> responses = getSpecialtiesUseCase.list(workshopId, workshopId, activeOnly).stream()
                .map(SpecialtyResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{specialtyId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER', 'SERVICE_ADVISOR', 'ASESOR', 'TECHNICIAN', 'MECANICO')")
    @Operation(summary = "Obtener Especialidad", description = "Detalle de una especialidad. Valida pertenencia al taller")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        SpecialtyResult result = getSpecialtiesUseCase.getById(workshopId, workshopId, specialtyId);
        return ResponseEntity.ok(SpecialtyResponse.from(result));
    }

    @PutMapping("/{specialtyId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Actualizar Especialidad", description = "Actualiza nombre y descripción de la especialidad")
    public ResponseEntity<SpecialtyResponse> updateSpecialty(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId,
            @Valid @RequestBody UpdateSpecialtyRequest request,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        UpdateSpecialtyCommand command = new UpdateSpecialtyCommand(
                workshopId,
                workshopId,
                specialtyId,
                request.name(),
                request.description(),
                resolveActor(authentication)
        );

        SpecialtyResult result = manageSpecialtyUseCase.update(command);
        return ResponseEntity.ok(SpecialtyResponse.from(result));
    }

    @DeleteMapping("/{specialtyId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Desactivar Especialidad", description = "Desactiva (soft-delete) la especialidad")
    public ResponseEntity<Void> deactivateSpecialty(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        manageSpecialtyUseCase.deactivate(workshopId, workshopId, specialtyId, resolveActor(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{specialtyId}/history")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Historial de Revisiones", description = "Auditoría cronológica de cambios en la especialidad")
    public ResponseEntity<List<SpecialtyRevisionResponse>> getSpecialtyHistory(
            @PathVariable UUID workshopId,
            @PathVariable UUID specialtyId,
            Authentication authentication) {
        verificarAcceso(workshopId, authentication);

        List<SpecialtyRevisionResponse> responses = getSpecialtiesUseCase.getHistory(workshopId, workshopId, specialtyId).stream()
                .map(SpecialtyRevisionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private void verificarAcceso(UUID tenantIdSolicitado, Authentication authentication) {
        UUID tenantDelContexto = TenantContextHolder.getTenantId();
        if (tenantDelContexto == null) {
            throw new TenantContextMissingException(
                    "La operacion requiere un contexto de taller establecido y no lo tiene.");
        }
        if (tenantDelContexto.equals(tenantIdSolicitado)) {
            return;
        }
        if (esSuperAdmin(authentication)) {
            return;
        }
        throw new TenantAccessDeniedException(tenantDelContexto, tenantIdSolicitado);
    }

    private boolean esSuperAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPERADMIN".equals(authority.getAuthority()));
    }

    private String resolveActor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "SYSTEM";
        }
        return authentication.getName();
    }
}
