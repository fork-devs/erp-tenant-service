package com.erp.tenant.interfaces.rest.dto.request;

public record UpdateEmployeeProfileRequest(
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {}
