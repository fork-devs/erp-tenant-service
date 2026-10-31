package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyRevisionJpaEntity;

public final class WorkshopSpecialtyRevisionPersistenceMapper {

    private WorkshopSpecialtyRevisionPersistenceMapper() {}

    public static WorkshopSpecialtyRevision toDomain(WorkshopSpecialtyRevisionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WorkshopSpecialtyRevision(
                entity.getId(),
                entity.getTenantId(),
                entity.getSpecialtyId(),
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getChangedBy(),
                entity.getChangedAt()
        );
    }

    public static WorkshopSpecialtyRevisionJpaEntity toEntity(WorkshopSpecialtyRevision domain) {
        if (domain == null) {
            return null;
        }
        return new WorkshopSpecialtyRevisionJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getSpecialtyId(),
                domain.getFieldName(),
                domain.getOldValue(),
                domain.getNewValue(),
                domain.getChangedBy(),
                domain.getChangedAt()
        );
    }
}
