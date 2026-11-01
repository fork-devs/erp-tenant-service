package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.command.UpdateWorkshopCommand;
import com.erp.tenant.application.dto.result.WorkshopResult;
import com.erp.tenant.application.usecase.ManageWorkshopUseCase;
import com.erp.tenant.application.usecase.ProvisionTenantUseCase;
import com.erp.tenant.interfaces.rest.dto.request.CreateWorkshopRequest;
import com.erp.tenant.interfaces.rest.dto.request.UpdateWorkshopRequest;
import com.erp.tenant.interfaces.rest.dto.response.WorkshopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workshops")
@Tag(name = "Workshops", description = "Endpoints para la gestión y actualización de talleres automotrices")
public class WorkshopController {

    private final ManageWorkshopUseCase manageWorkshopUseCase;
    private final ProvisionTenantUseCase provisionTenantUseCase;
    private final TenantRepositoryPort tenantRepositoryPort;
    private final com.erp.tenant.domain.port.IdentityValidationPort identityValidationPort;

    public WorkshopController(ManageWorkshopUseCase manageWorkshopUseCase) {
        this(manageWorkshopUseCase, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WorkshopController(ManageWorkshopUseCase manageWorkshopUseCase,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) ProvisionTenantUseCase provisionTenantUseCase,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) TenantRepositoryPort tenantRepositoryPort,
                              @org.springframework.beans.factory.annotation.Autowired(required = false) com.erp.tenant.domain.port.IdentityValidationPort identityValidationPort) {
        this.manageWorkshopUseCase = manageWorkshopUseCase;
        this.provisionTenantUseCase = provisionTenantUseCase;
        this.tenantRepositoryPort = tenantRepositoryPort;
        this.identityValidationPort = identityValidationPort;
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("isAuthenticated() and (hasAnyRole('SUPERADMIN', 'ADMIN', 'WORKSHOP_ADMIN', 'ADMIN_TALLER') or @multiTenancySecurityService.isAuthorizedForOwnerProfile(#request.ownerId()))")
    @Operation(summary = "Crear Taller", description = "Crea un nuevo taller automotriz asociado a un owner con validación estricta de SUNAT")
    public ResponseEntity<WorkshopResponse> createWorkshop(@Valid @RequestBody CreateWorkshopRequest request) {
        String businessName = request.businessName();
        String brandName = request.brandName();

        if (identityValidationPort != null) {
            com.erp.tenant.domain.port.RucValidationResult rucResult = identityValidationPort.validateRuc(request.taxId());
            if (rucResult == null || !rucResult.valid()) {
                throw new com.erp.tenant.domain.exception.RucNotActiveException(request.taxId());
            }
            if (!rucResult.isActiveAndHabido()) {
                throw new com.erp.tenant.domain.exception.RucNotActiveException(request.taxId(), rucResult.status(), rucResult.condition());
            }
            if (rucResult.legalName() != null && !rucResult.legalName().isBlank()) {
                businessName = rucResult.legalName();
            }
            if (rucResult.tradeName() != null && !rucResult.tradeName().isBlank() && (brandName == null || brandName.isBlank())) {
                brandName = rucResult.tradeName();
            }
        }

        UUID workshopId = null;
        if (tenantRepositoryPort != null) {
            var existing = tenantRepositoryPort.findByRuc(Ruc.of(request.taxId()));
            if (existing.isPresent()) {
                workshopId = existing.get().getId().getValue();
            }
        }
        if (workshopId == null && provisionTenantUseCase != null) {
            var res = provisionTenantUseCase.provisionTenant(new ProvisionTenantCommand(
                    brandName, request.taxId(), "ENTERPRISE", null, "#000000"
            ));
            workshopId = res.id();
        }
        if (workshopId == null) {
            workshopId = UUID.randomUUID();
        }
        if (tenantRepositoryPort != null) {
            var tenantOpt = tenantRepositoryPort.findById(new TenantId(workshopId));
            if (tenantOpt.isPresent()) {
                var tenant = tenantOpt.get();
                tenant.setOwnerId(request.ownerId());
                tenantRepositoryPort.save(tenant);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new WorkshopResponse(
                workshopId,
                request.ownerId(),
                businessName,
                brandName,
                request.taxId(),
                request.mileageIntervalConfig() != null ? request.mileageIntervalConfig() : 5000
        ));
    }


    @PutMapping("/{workshopId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Actualizar Taller", description = "Actualiza datos de razón social, nombre comercial e intervalo de alertas")
    public ResponseEntity<WorkshopResponse> updateWorkshop(
            @PathVariable UUID workshopId,
            @RequestBody UpdateWorkshopRequest request) {
        UpdateWorkshopCommand command = new UpdateWorkshopCommand(
                workshopId,
                request.businessName(),
                request.brandName(),
                request.taxId(),
                request.mileageIntervalConfig()
        );
        WorkshopResult result = manageWorkshopUseCase.update(command);
        return ResponseEntity.ok(WorkshopResponse.from(result));
    }

    @GetMapping("/{workshopId}")
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")
    @Operation(summary = "Obtener Taller por ID", description = "Retorna detalle del taller validando acceso")
    public ResponseEntity<WorkshopResponse> getWorkshopById(@PathVariable UUID workshopId) {
        WorkshopResult result = manageWorkshopUseCase.getById(workshopId);
        return ResponseEntity.ok(WorkshopResponse.from(result));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForOwnerProfile(#ownerId)")
    @Operation(summary = "Listar Talleres del Dueño", description = "Lista todos los talleres de un owner")
    public ResponseEntity<List<WorkshopResponse>> listWorkshopsByOwner(@RequestParam(name = "ownerId") UUID ownerId) {
        List<WorkshopResponse> responses = manageWorkshopUseCase.listByOwner(ownerId).stream()
                .map(WorkshopResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
