package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.OwnerProfileResult;

import java.util.UUID;

public record OwnerProfileResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {
    public static OwnerProfileResponse from(OwnerProfileResult result) {
        if (result == null) return null;
        return new OwnerProfileResponse(
                result.id(),
                result.userId(),
                result.firstName(),
                result.lastName(),
                result.documentType(),
                result.documentNumber(),
                result.phone()
        );
    }
}
