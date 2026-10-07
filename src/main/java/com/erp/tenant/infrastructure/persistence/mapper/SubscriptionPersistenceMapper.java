package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.Subscription;
import com.erp.tenant.infrastructure.persistence.entity.SubscriptionJpaEntity;

public final class SubscriptionPersistenceMapper {

    private SubscriptionPersistenceMapper() {}

    public static Subscription toDomain(SubscriptionJpaEntity entity) {
        if (entity == null) return null;
        return new Subscription(
                entity.getId(),
                entity.getTenantId(),
                entity.getPlanId(),
                entity.getStripeSubscriptionId(),
                entity.getStatus(),
                entity.getCurrentPeriodEnd(),
                entity.getCreatedAt()
        );
    }

    public static SubscriptionJpaEntity toEntity(Subscription domain) {
        if (domain == null) return null;
        return new SubscriptionJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getPlanId(),
                domain.getStripeSubscriptionId(),
                domain.getStatus(),
                domain.getCurrentPeriodEnd(),
                domain.getCreatedAt()
        );
    }
}
