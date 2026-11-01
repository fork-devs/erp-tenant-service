package com.erp.tenant.interfaces.rest.dto.response;

import com.erp.tenant.application.dto.result.SpecialtyRevisionResult;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyRevisionResponse(
        UUID id,
        UUID specialtyId,
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        Instant changedAt
) {
    public static SpecialtyRevisionResponse from(SpecialtyRevisionResult result) {
        if (result == null) return null;
        return new SpecialtyRevisionResponse(
                result.id(),
                result.specialtyId(),
                result.fieldName(),
                result.oldValue(),
                result.newValue(),
                result.changedBy(),
                result.changedAt()
        );
    }
}
