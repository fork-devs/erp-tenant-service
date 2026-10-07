package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;

public interface ProvisionTenantUseCase {
    TenantResult provisionTenant(ProvisionTenantCommand command);
}
