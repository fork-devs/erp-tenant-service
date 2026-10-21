package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.model.TenantOutboxStatus;
import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;

public final class TenantOutboxPersistenceMapper {

    private TenantOutboxPersistenceMapper() {}

    public static TenantOutboxMessage toDomain(TenantOutboxJpaEntity entity) {
        if (entity == null) return null;
        return new TenantOutboxMessage(
                entity.getId(),
                entity.getEventId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getTenantId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getAttempts() != null ? entity.getAttempts() : 0,
                entity.getNextAttemptAt(),
                entity.getLastError(),
                entity.getStatus() != null ? entity.getStatus() : TenantOutboxStatus.PENDING,
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
                domain.getEventId(),
                domain.getEventType(),
                domain.getPayload(),
                domain.isPublished(),
                domain.getAttempts(),
                domain.getNextAttemptAt(),
                domain.getLastError(),
                domain.getStatus(),
                domain.getCreatedAt()
        );
    }
}
