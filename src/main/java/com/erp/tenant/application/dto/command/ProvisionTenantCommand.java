package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record ProvisionTenantCommand(
        String commercialName,
        String ruc,
        String planCode,
        UUID planId,
        String brandColor
) {}
