package com.erp.tenant.application.dto.result;

import java.util.UUID;

public record TenantLimitsResult(
        UUID tenantId,
        String status,
        String planCode,
        int maxUsers,
        int maxVehicles,
        int maxIotMsgPerDay
) {}
