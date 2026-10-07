package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.dto.result.PlanResult;
import com.erp.tenant.application.usecase.ListPlansUseCase;
import com.erp.tenant.interfaces.rest.dto.response.PlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Endpoints para consulta de planes de suscripción disponibles")
public class PlanController {

    private final ListPlansUseCase listPlansUseCase;

    public PlanController(ListPlansUseCase listPlansUseCase) {
        this.listPlansUseCase = listPlansUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar todos los planes de suscripción configurados")
    public ResponseEntity<List<PlanResponse>> listPlans() {
        List<PlanResponse> responses = listPlansUseCase.listAllPlans().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private PlanResponse toResponse(PlanResult result) {
        return new PlanResponse(
                result.id(),
                result.code(),
                result.maxUsers(),
                result.maxVehicles(),
                result.maxIotMsgPerDay(),
                result.priceMonthly()
        );
    }
}
