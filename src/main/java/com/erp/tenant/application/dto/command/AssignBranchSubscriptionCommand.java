package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record AssignBranchSubscriptionCommand(
        UUID branchId,
        UUID planId,
        String billingCycle
) {}
