package com.erp.tenant.interfaces.rest.dto.response;

import java.util.UUID;

public record TenantLimitsResponse(
        UUID tenantId,
        String status,
        String planCode,
        int maxUsers,
        int maxVehicles,
        int maxIotMsgPerDay,
        int maxRequestsPerMinute,
        int maxConcurrentSlots
) {}
