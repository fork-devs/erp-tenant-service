package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.BranchSubscription;
import com.erp.tenant.infrastructure.persistence.entity.BranchSubscriptionJpaEntity;

public final class BranchSubscriptionPersistenceMapper {

    private BranchSubscriptionPersistenceMapper() {}

    public static BranchSubscription toDomain(BranchSubscriptionJpaEntity entity) {
        if (entity == null) return null;
        return new BranchSubscription(
                entity.getId(),
                entity.getTenantId(),
                entity.getBranchId(),
                entity.getPlanId(),
                entity.getBillingCycle(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt()
        );
    }

    public static BranchSubscriptionJpaEntity toEntity(BranchSubscription domain) {
        if (domain == null) return null;
        return new BranchSubscriptionJpaEntity(
                domain.getId(),
                domain.getTenantId(),
                domain.getBranchId(),
                domain.getPlanId(),
                domain.getBillingCycle(),
                domain.getStatus(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getCreatedAt()
        );
    }
}
