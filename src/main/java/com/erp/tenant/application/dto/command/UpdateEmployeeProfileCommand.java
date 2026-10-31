package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateEmployeeProfileCommand(
        UUID employeeId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {}
