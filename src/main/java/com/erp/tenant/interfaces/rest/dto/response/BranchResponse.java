package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.BranchResult;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        UUID workshopId,
        String code,
        String name,
        String address,
        String phone
) {
    public static BranchResponse from(BranchResult result) {
        if (result == null) return null;
        return new BranchResponse(
                result.id(),
                result.workshopId(),
                result.code(),
                result.name(),
                result.address(),
                result.phone()
        );
    }
}
