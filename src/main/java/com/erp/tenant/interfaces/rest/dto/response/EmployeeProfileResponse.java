package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.EmployeeProfileResult;

import java.util.UUID;

public record EmployeeProfileResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {
    public static EmployeeProfileResponse from(EmployeeProfileResult result) {
        if (result == null) return null;
        return new EmployeeProfileResponse(
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
