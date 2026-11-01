package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateWorkshopCommand(
        UUID workshopId,
        String businessName,
        String brandName,
        String taxId,
        Integer mileageIntervalConfig
) {}
