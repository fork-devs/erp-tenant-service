package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.ChangePlanCommand;
import com.erp.tenant.application.dto.result.TenantResult;

public interface ChangeTenantPlanUseCase {
    TenantResult changePlan(ChangePlanCommand command);
}
