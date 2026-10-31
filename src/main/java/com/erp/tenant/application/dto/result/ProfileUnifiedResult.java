package com.erp.tenant.application.dto.result;

import java.util.List;
import java.util.UUID;

public record ProfileUnifiedResult(
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
) {}
