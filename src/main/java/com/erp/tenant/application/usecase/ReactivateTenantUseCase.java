package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.TenantResult;

import java.util.UUID;

public interface ReactivateTenantUseCase {
    TenantResult reactivateTenant(UUID tenantId);
}
