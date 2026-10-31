package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.command.UpdateWorkshopCommand;
import com.erp.tenant.application.dto.result.WorkshopResult;
import com.erp.tenant.application.usecase.ManageWorkshopUseCase;
import com.erp.tenant.interfaces.rest.dto.request.UpdateWorkshopRequest;
import com.erp.tenant.interfaces.rest.dto.response.WorkshopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workshops")
@Tag(name = "Workshops", description = "Endpoints para la gestión y actualización de talleres automotrices")
public class WorkshopController {

    private final ManageWorkshopUseCase manageWorkshopUseCase;

    public WorkshopController(ManageWorkshopUseCase manageWorkshopUseCase) {
        this.manageWorkshopUseCase = manageWorkshopUseCase;
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
