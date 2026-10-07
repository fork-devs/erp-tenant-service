package com.erp.tenant.interfaces.rest.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String commercialName,
        String ruc,
        UUID planId,
        String planCode,
        String status,
        String brandColor,
        Instant createdAt
) {}
