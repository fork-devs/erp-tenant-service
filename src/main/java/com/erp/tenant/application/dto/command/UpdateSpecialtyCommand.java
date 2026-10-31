package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateSpecialtyCommand(
        UUID tenantId,
        UUID workshopId,
        UUID specialtyId,
        String name,
        String description,
        String actorId
) {
    public UpdateSpecialtyCommand {
        if (tenantId == null) throw new IllegalArgumentException("tenantId es obligatorio");
        if (workshopId == null) throw new IllegalArgumentException("workshopId es obligatorio");
        if (specialtyId == null) throw new IllegalArgumentException("specialtyId es obligatorio");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
    }
}
