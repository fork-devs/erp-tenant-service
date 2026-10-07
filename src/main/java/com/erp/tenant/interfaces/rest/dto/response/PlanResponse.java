package com.erp.tenant.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String code,
        int maxUsers,
        int maxVehicles,
        int maxIotMsgPerDay,
        BigDecimal priceMonthly
) {}
