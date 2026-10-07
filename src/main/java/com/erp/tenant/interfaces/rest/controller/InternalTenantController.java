package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.result.TenantLimitsResult;
import com.erp.tenant.application.usecase.GetTenantDetailsUseCase;
import com.erp.tenant.interfaces.rest.dto.response.TenantLimitsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/tenants")
@Tag(name = "Internal Tenants", description = "Endpoints internos para comunicación inter-servicios (Gateway, Auth, Telemetry)")
public class InternalTenantController {

    private final GetTenantDetailsUseCase getTenantDetailsUseCase;

    public InternalTenantController(GetTenantDetailsUseCase getTenantDetailsUseCase) {
        this.getTenantDetailsUseCase = getTenantDetailsUseCase;
    }

    @GetMapping("/{id}/limits")
    @Operation(summary = "Consultar cuotas y límites de un taller (para Gateway y Rate Limiting)")
    public ResponseEntity<TenantLimitsResponse> getTenantLimits(@PathVariable UUID id) {
        TenantLimitsResult result = getTenantDetailsUseCase.getLimitsById(id);
        TenantLimitsResponse response = new TenantLimitsResponse(
                result.tenantId(),
                result.status(),
                result.planCode(),
                result.maxUsers(),
                result.maxVehicles(),
                result.maxIotMsgPerDay()
        );
        return ResponseEntity.ok(response);
    }
}
