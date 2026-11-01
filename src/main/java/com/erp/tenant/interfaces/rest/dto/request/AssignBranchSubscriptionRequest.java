package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignBranchSubscriptionRequest(
        @NotNull(message = "planId is required")
        UUID planId,

        @NotBlank(message = "billingCycle is required (MONTHLY or ANNUAL)")
        String billingCycle
) {}
