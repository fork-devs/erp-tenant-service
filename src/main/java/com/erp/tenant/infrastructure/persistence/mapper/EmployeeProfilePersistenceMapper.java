package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.DocumentType;
import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.infrastructure.persistence.entity.EmployeeProfileJpaEntity;

public final class EmployeeProfilePersistenceMapper {

    private EmployeeProfilePersistenceMapper() {}

    public static EmployeeProfile toDomain(EmployeeProfileJpaEntity entity) {
        if (entity == null) return null;
        return new EmployeeProfile(
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

    public static EmployeeProfileJpaEntity toEntity(EmployeeProfile domain) {
        if (domain == null) return null;
        return new EmployeeProfileJpaEntity(
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
