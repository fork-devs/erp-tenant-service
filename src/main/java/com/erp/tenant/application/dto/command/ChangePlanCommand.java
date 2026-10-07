package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record ChangePlanCommand(
        UUID tenantId,
        UUID newPlanId,
        String newPlanCode
) {}
