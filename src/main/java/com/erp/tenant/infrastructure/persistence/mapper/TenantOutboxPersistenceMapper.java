package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;

public final class TenantOutboxPersistenceMapper {

    private TenantOutboxPersistenceMapper() {}

    public static TenantOutboxMessage toDomain(TenantOutboxJpaEntity entity) {
        if (entity == null) return null;
        return new TenantOutboxMessage(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getTenantId(),
                entity.getEventType(),
                entity.getPayload(),
                Boolean.TRUE.equals(entity.getPublished()),
                entity.getCreatedAt()
        );
    }

    public static TenantOutboxJpaEntity toEntity(TenantOutboxMessage domain) {
        if (domain == null) return null;
        return new TenantOutboxJpaEntity(
                domain.getId(),
                domain.getAggregateType(),
                domain.getAggregateId(),
                domain.getTenantId(),
                domain.getEventType(),
                domain.getPayload(),
                domain.isPublished(),
                domain.getCreatedAt()
        );
    }
}
