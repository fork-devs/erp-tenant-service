package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateWorkshopRequest(
        @NotNull UUID ownerId,
        @NotBlank String businessName,
        @NotBlank String brandName,
        @NotBlank String taxId,
        Integer mileageIntervalConfig
) {}
