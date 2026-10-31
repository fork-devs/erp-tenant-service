package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record CreateOwnerProfileCommand(
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {}
