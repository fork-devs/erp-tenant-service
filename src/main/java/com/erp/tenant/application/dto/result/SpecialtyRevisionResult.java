package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyRevisionResult(
        UUID id,
        UUID specialtyId,
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        Instant changedAt
) {
    public static SpecialtyRevisionResult from(WorkshopSpecialtyRevision rev) {
        if (rev == null) return null;
        return new SpecialtyRevisionResult(
                rev.getId(),
                rev.getSpecialtyId(),
                rev.getFieldName(),
                rev.getOldValue(),
                rev.getNewValue(),
                rev.getChangedBy(),
                rev.getChangedAt()
        );
    }
}
