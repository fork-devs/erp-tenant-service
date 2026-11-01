package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.WorkshopSpecialty;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyResult(
        UUID id,
        UUID workshopId,
        String name,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static SpecialtyResult from(WorkshopSpecialty specialty) {
        if (specialty == null) return null;
        return new SpecialtyResult(
                specialty.getId(),
                specialty.getWorkshopId(),
                specialty.getName(),
                specialty.getCode(),
                specialty.getDescription(),
                specialty.isActive(),
                specialty.getCreatedAt(),
                specialty.getUpdatedAt()
        );
    }
}
