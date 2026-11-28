package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record UpdateBranchRequest(
        String code,
        String name,
        String address,
        String phone,

        @DecimalMin(value = "-90.0", message = "latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90.0")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180.0")
        Double longitude,

        String coverImageUrl
) {
    public UpdateBranchRequest(String code, String name, String address, String phone) {
        this(code, name, address, phone, null, null, null);
    }

    public UpdateBranchRequest(String code, String name, String address, String phone, Double latitude, Double longitude) {
        this(code, name, address, phone, latitude, longitude, null);
    }
}
