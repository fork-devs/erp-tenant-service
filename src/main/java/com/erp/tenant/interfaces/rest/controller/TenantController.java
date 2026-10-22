package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.ChangePlanCommand;
import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.command.UpdateBrandingCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.usecase.ChangeTenantPlanUseCase;
import com.erp.tenant.application.usecase.GetTenantDetailsUseCase;
import com.erp.tenant.application.usecase.ProvisionTenantUseCase;
import com.erp.tenant.application.usecase.ReactivateTenantUseCase;
import com.erp.tenant.application.usecase.SuspendTenantUseCase;
import com.erp.tenant.application.usecase.UpdateBrandingUseCase;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.domain.exception.TenantContextMissingException;
import com.erp.tenant.infrastructure.security.TenantContextHolder;
import com.erp.tenant.interfaces.rest.dto.request.ChangePlanRequest;
import com.erp.tenant.interfaces.rest.dto.request.ProvisionTenantRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateBrandingRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateTenantStatusRequest;
import com.erp.tenant.interfaces.rest.dto.response.TenantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Endpoints de gestion de talleres.
 *
 * <p>Cada operacion comprueba que el taller solicitado sea el del principal
 * autenticado. La excepcion es el superadministrador de la plataforma, que
 * opera con alcance transversal.
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Endpoints para el aprovisionamiento y gestión de talleres (inquilinos)")
public class TenantController {

    private final ProvisionTenantUseCase provisionTenantUseCase;
    private final ChangeTenantPlanUseCase changeTenantPlanUseCase;
    private final SuspendTenantUseCase suspendTenantUseCase;
    private final ReactivateTenantUseCase reactivateTenantUseCase;
    private final UpdateBrandingUseCase updateBrandingUseCase;
    private final GetTenantDetailsUseCase getTenantDetailsUseCase;

    public TenantController(
            ProvisionTenantUseCase provisionTenantUseCase,
            ChangeTenantPlanUseCase changeTenantPlanUseCase,
            SuspendTenantUseCase suspendTenantUseCase,
            ReactivateTenantUseCase reactivateTenantUseCase,
            UpdateBrandingUseCase updateBrandingUseCase,
            GetTenantDetailsUseCase getTenantDetailsUseCase) {
        this.provisionTenantUseCase = provisionTenantUseCase;
        this.changeTenantPlanUseCase = changeTenantPlanUseCase;
        this.suspendTenantUseCase = suspendTenantUseCase;
        this.reactivateTenantUseCase = reactivateTenantUseCase;
        this.updateBrandingUseCase = updateBrandingUseCase;
        this.getTenantDetailsUseCase = getTenantDetailsUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Operation(summary = "Aprovisionar un nuevo taller/inquilino", description = "Crea el inquilino, asocia el plan y emite evento en transactional outbox")
    public ResponseEntity<TenantResponse> provisionTenant(@Valid @RequestBody ProvisionTenantRequest request) {
        ProvisionTenantCommand command = new ProvisionTenantCommand(
                request.commercialName(),
                request.ruc(),
                request.planCode(),
                request.planId(),
                request.brandColor()
        );

        TenantResult result = provisionTenantUseCase.provisionTenant(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Operation(summary = "Listar todos los inquilinos")
    public ResponseEntity<List<TenantResponse>> listTenants() {
        List<TenantResponse> responses = getTenantDetailsUseCase.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalles de un taller por su ID")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id, Authentication authentication) {
        verificarAcceso(id, authentication);
        TenantResult result = getTenantDetailsUseCase.getById(id);
        return ResponseEntity.ok(toResponse(result));
    }

    @PutMapping("/{id}/plan")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Operation(summary = "Cambiar plan de suscripción del taller")
    public ResponseEntity<TenantResponse> changePlan(
            @PathVariable UUID id,
            @RequestBody ChangePlanRequest request) {
        ChangePlanCommand command = new ChangePlanCommand(
                id,
                request.newPlanId(),
                request.newPlanCode()
        );
        TenantResult result = changeTenantPlanUseCase.changePlan(command);
        return ResponseEntity.ok(toResponse(result));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Suspender o reactivar un taller")
    public ResponseEntity<TenantResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantStatusRequest request,
            Authentication authentication) {
        verificarAcceso(id, authentication);

        TenantResult result;
        if ("SUSPEND".equalsIgnoreCase(request.action())) {
            result = suspendTenantUseCase.suspendTenant(id, request.reason());
        } else if ("REACTIVATE".equalsIgnoreCase(request.action())) {
            result = reactivateTenantUseCase.reactivateTenant(id);
        } else {
            throw new IllegalArgumentException("Acción de estado inválida: " + request.action() + ". Use SUSPEND o REACTIVATE.");
        }
        return ResponseEntity.ok(toResponse(result));
    }

    @PatchMapping("/{id}/branding")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Actualizar personalización visual (Branding)")
    public ResponseEntity<TenantResponse> updateBranding(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandingRequest request,
            Authentication authentication) {
        verificarAcceso(id, authentication);

        UpdateBrandingCommand command = new UpdateBrandingCommand(
                id,
                request.commercialName(),
                request.brandColor()
        );
        TenantResult result = updateBrandingUseCase.updateBranding(command);
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * Un taller solo puede operar sobre si mismo. El superadministrador de la
     * plataforma opera con alcance transversal.
     */
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

    private TenantResponse toResponse(TenantResult result) {
        return new TenantResponse(
                result.id(),
                result.commercialName(),
                result.ruc(),
                result.planId(),
                result.planCode(),
                result.status(),
                result.brandColor(),
                result.createdAt()
        );
    }
}
