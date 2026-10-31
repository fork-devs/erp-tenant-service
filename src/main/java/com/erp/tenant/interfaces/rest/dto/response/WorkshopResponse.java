package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.WorkshopResult;

import java.util.UUID;

public record WorkshopResponse(
        UUID id,
        UUID ownerId,
        String businessName,
        String brandName,
        String taxId,
        Integer mileageIntervalConfig
) {
    public static WorkshopResponse from(WorkshopResult result) {
        if (result == null) return null;
        return new WorkshopResponse(
                result.id(),
                result.ownerId(),
                result.businessName(),
                result.brandName(),
                result.taxId(),
                result.mileageIntervalConfig()
        );
    }
}
