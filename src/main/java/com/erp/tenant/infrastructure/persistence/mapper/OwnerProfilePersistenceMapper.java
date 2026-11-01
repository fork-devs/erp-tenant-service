package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;

public final class OwnerProfilePersistenceMapper {

    private OwnerProfilePersistenceMapper() {}

    public static OwnerProfile toDomain(OwnerProfileJpaEntity entity) {
        if (entity == null) return null;
        return new OwnerProfile(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                DocumentType.fromString(entity.getDocumentType()),
                entity.getDocumentNumber(),
                entity.getPhone(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static OwnerProfileJpaEntity toEntity(OwnerProfile domain) {
        if (domain == null) return null;
        return new OwnerProfileJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getUserId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getDocumentType().name(),
                domain.getDocumentNumber(),
                domain.getPhone(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
