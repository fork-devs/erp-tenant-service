package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.CreateBranchCommand;
import com.erp.tenant.application.dto.command.UpdateBranchCommand;
import com.erp.tenant.application.dto.result.BranchResult;

import java.util.UUID;

public interface ManageBranchUseCase {
    BranchResult create(CreateBranchCommand command);
    BranchResult update(UpdateBranchCommand command);
    void deactivate(UUID branchId);
}
