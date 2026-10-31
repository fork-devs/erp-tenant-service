package com.erp.tenant.interfaces.rest.dto.request;

public record UpdateWorkshopRequest(
        String businessName,
        String brandName,
        String taxId,
        Integer mileageIntervalConfig
) {}
