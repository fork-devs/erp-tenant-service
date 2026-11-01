package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.BranchSubscriptionResult;

import java.time.Instant;
import java.util.UUID;

public record BranchSubscriptionResponse(
        UUID id,
        UUID branchId,
        UUID planId,
        String billingCycle,
        String status,
        Instant startDate,
        Instant endDate
) {
    public static BranchSubscriptionResponse from(BranchSubscriptionResult result) {
        if (result == null) return null;
        return new BranchSubscriptionResponse(
                result.id(),
                result.branchId(),
                result.planId(),
                result.billingCycle(),
                result.status(),
                result.startDate(),
                result.endDate()
        );
    }
}
