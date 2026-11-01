package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateOwnerProfileCommand(
        UUID ownerId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone
) {}
