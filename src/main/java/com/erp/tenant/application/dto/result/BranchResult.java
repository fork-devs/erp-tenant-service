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
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static BranchResult from(Branch branch) {
        if (branch == null) return null;
        return new BranchResult(
                branch.getId(),
                branch.getWorkshopId(),
                branch.getCode(),
                branch.getName(),
                branch.getAddress(),
                branch.getPhone(),
                branch.isActive(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}
