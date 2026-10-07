package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.TenantLimitsResult;
import com.erp.tenant.application.dto.result.TenantResult;

import java.util.List;
import java.util.UUID;

public interface GetTenantDetailsUseCase {
    TenantResult getById(UUID tenantId);
    TenantLimitsResult getLimitsById(UUID tenantId);
    List<TenantResult> listAll();
}
