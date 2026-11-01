package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.BranchResult;

import java.util.List;
import java.util.UUID;

public interface GetBranchesUseCase {
    BranchResult getById(UUID branchId);
    List<BranchResult> listByWorkshop(UUID workshopId);
}
