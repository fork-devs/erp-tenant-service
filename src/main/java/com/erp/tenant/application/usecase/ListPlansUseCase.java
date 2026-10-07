package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.PlanResult;

import java.util.List;

public interface ListPlansUseCase {
    List<PlanResult> listAllPlans();
}
