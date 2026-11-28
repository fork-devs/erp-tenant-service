package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.Branch;

import java.time.Instant;
import java.util.UUID;

public record BranchResult(
        UUID id,
        UUID workshopId,
        String code,
        String name,
        String address,
        String phone,
        String bannerImageUrl,
        Double latitude,
        Double longitude,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public BranchResult(
            UUID id,
            UUID workshopId,
            String code,
            String name,
            String address,
            String phone,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, workshopId, code, name, address, phone, null, null, null, active, createdAt, updatedAt);
    }

    public BranchResult(
            UUID id,
            UUID workshopId,
            String code,
            String name,
            String address,
            String phone,
            Double latitude,
            Double longitude,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, workshopId, code, name, address, phone, null, latitude, longitude, active, createdAt, updatedAt);
    }

    public static BranchResult from(Branch branch) {
        if (branch == null) return null;
        return new BranchResult(
                branch.getId(),
                branch.getWorkshopId(),
                branch.getCode(),
                branch.getName(),
                branch.getAddress(),
                branch.getPhone(),
                branch.getBannerImageUrl(),
                branch.getLatitude(),
                branch.getLongitude(),
                branch.isActive(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}
