package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.SpecialtyResult;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        UUID workshopId,
        String name,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static SpecialtyResponse from(SpecialtyResult result) {
        if (result == null) return null;
        return new SpecialtyResponse(
                result.id(),
                result.workshopId(),
                result.name(),
                result.code(),
                result.description(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
