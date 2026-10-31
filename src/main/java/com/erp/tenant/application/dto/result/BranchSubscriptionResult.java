package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.BranchSubscription;

import java.time.Instant;
import java.util.UUID;

public record BranchSubscriptionResult(
        UUID id,
        UUID branchId,
        UUID planId,
        String billingCycle,
        String status,
        Instant startDate,
        Instant endDate
) {
    public static BranchSubscriptionResult from(BranchSubscription sub) {
        if (sub == null) return null;
        return new BranchSubscriptionResult(
                sub.getId(),
                sub.getBranchId(),
                sub.getPlanId(),
                sub.getBillingCycle(),
                sub.getStatus(),
                sub.getStartDate(),
                sub.getEndDate()
        );
    }
}
