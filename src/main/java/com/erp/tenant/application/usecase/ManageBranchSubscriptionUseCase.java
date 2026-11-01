package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.AssignBranchSubscriptionCommand;
import com.erp.tenant.application.dto.result.BranchSubscriptionResult;

import java.util.UUID;

public interface ManageBranchSubscriptionUseCase {
    BranchSubscriptionResult assignSubscription(AssignBranchSubscriptionCommand command);
    void cancelSubscription(UUID branchId);
}
