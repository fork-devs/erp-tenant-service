package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateBrandingCommand(
        UUID tenantId,
        String commercialName,
        String brandColor
) {}
