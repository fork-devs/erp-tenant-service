package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.WorkshopSpecialty;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyJpaEntity;

public final class WorkshopSpecialtyPersistenceMapper {

    private WorkshopSpecialtyPersistenceMapper() {}

    public static WorkshopSpecialty toDomain(WorkshopSpecialtyJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WorkshopSpecialty(
                entity.getId(),
                entity.getTenantId(),
                entity.getWorkshopId(),
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static WorkshopSpecialtyJpaEntity toEntity(WorkshopSpecialty domain) {
        if (domain == null) {
            return null;
        }
        return new WorkshopSpecialtyJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getWorkshopId(),
                domain.getName(),
                domain.getCode(),
                domain.getDescription(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
