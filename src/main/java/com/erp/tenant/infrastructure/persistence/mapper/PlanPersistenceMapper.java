package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.infrastructure.persistence.entity.PlanJpaEntity;

public final class PlanPersistenceMapper {

    private PlanPersistenceMapper() {}

    public static Plan toDomain(PlanJpaEntity entity) {
        if (entity == null) return null;
        return new Plan(
                entity.getId(),
                PlanCode.fromString(entity.getCode()),
                entity.getMaxUsers(),
                entity.getMaxVehicles(),
                entity.getMaxIotMsgPerDay(),
                entity.getMaxRequestsPerMinute(),
                entity.getMaxConcurrentSlots(),
                entity.getPriceMonthly()
        );
    }

    public static PlanJpaEntity toEntity(Plan domain) {
        if (domain == null) return null;
        return new PlanJpaEntity(
                domain.getId(),
                domain.getCode().name(),
                domain.getMaxUsers(),
                domain.getMaxVehicles(),
                domain.getMaxIotMsgPerDay(),
                domain.getMaxRequestsPerMinute(),
                domain.getMaxConcurrentSlots(),
                domain.getPriceMonthly()
        );
    }
}
