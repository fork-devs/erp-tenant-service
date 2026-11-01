package com.erp.tenant.application.dto.result;

import com.erp.tenant.domain.model.OwnerProfile;

import java.time.Instant;
import java.util.UUID;

public record OwnerProfileResult(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String documentType,
        String documentNumber,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
    public static OwnerProfileResult from(OwnerProfile owner) {
        if (owner == null) return null;
        return new OwnerProfileResult(
                owner.getId(),
                owner.getUserId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getDocumentType().name(),
                owner.getDocumentNumber(),
                owner.getPhone(),
                owner.getCreatedAt(),
                owner.getUpdatedAt()
        );
    }
}
