package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBranchRequest(
        @NotNull(message = "workshopId is required")
        UUID workshopId,

        @NotBlank(message = "code is required")
        String code,

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "address is required")
        String address,

        @NotBlank(message = "phone is required")
        String phone,

        @DecimalMin(value = "-90.0", message = "latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90.0")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180.0")
        Double longitude,

        String coverImageUrl
) {
    public CreateBranchRequest(UUID workshopId, String code, String name, String address, String phone) {
        this(workshopId, code, name, address, phone, null, null, null);
    }

    public CreateBranchRequest(UUID workshopId, String code, String name, String address, String phone, Double latitude, Double longitude) {
        this(workshopId, code, name, address, phone, latitude, longitude, null);
    }
}
