package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.UpdateBrandingCommand;
import com.erp.tenant.application.dto.result.TenantResult;

public interface UpdateBrandingUseCase {
    TenantResult updateBranding(UpdateBrandingCommand command);
}
