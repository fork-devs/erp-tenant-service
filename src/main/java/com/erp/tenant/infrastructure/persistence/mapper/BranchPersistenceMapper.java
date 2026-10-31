package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.Branch;
import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;

public final class BranchPersistenceMapper {

    private BranchPersistenceMapper() {}

    public static Branch toDomain(BranchJpaEntity entity) {
        if (entity == null) return null;
        return new Branch(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkshopId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static BranchJpaEntity toEntity(Branch domain) {
        if (domain == null) return null;
        return new BranchJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getWorkshopId(),
                domain.getCode(),
                domain.getName(),
                domain.getAddress(),
                domain.getPhone(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
