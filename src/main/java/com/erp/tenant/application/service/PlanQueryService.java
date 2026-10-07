package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.result.PlanResult;
import com.erp.tenant.application.usecase.ListPlansUseCase;
import com.erp.tenant.domain.port.PlanRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

public class PlanQueryService implements ListPlansUseCase {

    private final PlanRepositoryPort planRepositoryPort;

    public PlanQueryService(PlanRepositoryPort planRepositoryPort) {
        this.planRepositoryPort = planRepositoryPort;
    }

    @Override
    public List<PlanResult> listAllPlans() {
        return planRepositoryPort.findAll().stream()
                .map(p -> new PlanResult(
                        p.getId(),
                        p.getCode().name(),
                        p.getMaxUsers(),
                        p.getMaxVehicles(),
                        p.getMaxIotMsgPerDay(),
                        p.getPriceMonthly()
                ))
                .collect(Collectors.toList());
    }
}
