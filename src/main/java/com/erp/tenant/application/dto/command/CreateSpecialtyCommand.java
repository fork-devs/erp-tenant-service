package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record CreateSpecialtyCommand(
        UUID tenantId,
        UUID workshopId,
        String name,
        String code,
        String description,
        String actorId
) {
    public CreateSpecialtyCommand {
        if (tenantId == null) throw new IllegalArgumentException("tenantId es obligatorio");
        if (workshopId == null) throw new IllegalArgumentException("workshopId es obligatorio");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("El código es obligatorio");
    }
}
