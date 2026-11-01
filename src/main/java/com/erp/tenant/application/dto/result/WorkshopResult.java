package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.Tenant;

import java.util.UUID;

public record WorkshopResult(
        UUID id,
        UUID ownerId,
        String businessName,
        String brandName,
        String taxId,
        Integer mileageIntervalConfig
) {
    public static WorkshopResult from(Tenant tenant) {
        if (tenant == null) return null;
        return new WorkshopResult(
                tenant.getId().getValue(),
                tenant.getOwnerId(),
                tenant.getCommercialName(),
                tenant.getCommercialName(),
                tenant.getRuc().getValue(),
                tenant.getMileageIntervalConfig()
        );
    }
}
