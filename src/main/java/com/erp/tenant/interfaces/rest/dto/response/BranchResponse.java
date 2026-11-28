package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.BranchResult;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        UUID workshopId,
        String code,
        String name,
        String address,
        String phone,
        String bannerImageUrl,
        String coverImageUrl,
        Double latitude,
        Double longitude
) {
    public BranchResponse(UUID id, UUID workshopId, String code, String name, String address, String phone) {
        this(id, workshopId, code, name, address, phone, null, null, null, null);
    }

    public BranchResponse(UUID id, UUID workshopId, String code, String name, String address, String phone, String bannerImageUrl) {
        this(id, workshopId, code, name, address, phone, bannerImageUrl, bannerImageUrl, null, null);
    }

    public BranchResponse(UUID id, UUID workshopId, String code, String name, String address, String phone,
                          String bannerImageUrl, Double latitude, Double longitude) {
        this(id, workshopId, code, name, address, phone, bannerImageUrl, bannerImageUrl, latitude, longitude);
    }

    public static BranchResponse from(BranchResult result) {
        if (result == null) return null;
        return new BranchResponse(
                result.id(),
                result.workshopId(),
                result.code(),
                result.name(),
                result.address(),
                result.phone(),
                result.bannerImageUrl(),
                result.bannerImageUrl(),
                result.latitude(),
                result.longitude()
        );
    }
}
