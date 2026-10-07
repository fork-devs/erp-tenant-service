package com.erp.tenant.application.dto.result;

import java.time.Instant;
import java.util.UUID;

public record TenantResult(
        UUID id,
        String commercialName,
        String ruc,
        UUID planId,
        String planCode,
        String status,
        String brandColor,
        Instant createdAt
) {}
