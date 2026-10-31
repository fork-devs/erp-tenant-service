package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;

import java.util.List;
import java.util.UUID;

public record ProfileUnifiedResponse(
        String profileType,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone,
        UUID employeeId,
        UUID ownerId,
        List<String> roles
) {
    public static ProfileUnifiedResponse from(ProfileUnifiedResult result) {
        if (result == null) return null;
        return new ProfileUnifiedResponse(
                result.profileType(),
                result.userId(),
                result.firstName(),
                result.lastName(),
                result.documentType(),
                result.documentNumber(),
                result.phone(),
                result.employeeId(),
                result.ownerId(),
                result.roles()
        );
    }
}
