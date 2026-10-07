package com.erp.tenant.application.dto.result;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResult(
        UUID id,
        String code,
        int maxUsers,
        int maxVehicles,
        int maxIotMsgPerDay,
        BigDecimal priceMonthly
) {}
