package com.erp.tenant.interfaces.rest.dto.request;

import java.util.UUID;

public record ChangePlanRequest(
        UUID newPlanId,
        String newPlanCode
) {}
