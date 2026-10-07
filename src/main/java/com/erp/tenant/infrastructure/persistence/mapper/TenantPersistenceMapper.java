package com.erp.tenant.infrastructure.persistence.mapper;

import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantStatus;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;
import com.erp.tenant.infrastructure.persistence.entity.TenantJpaEntity;

public final class TenantPersistenceMapper {

    private TenantPersistenceMapper() {}

    public static Tenant toDomain(TenantJpaEntity entity) {
        if (entity == null) return null;
        return new Tenant(
                new TenantId(entity.getId()),
                entity.getCommercialName(),
                Ruc.of(entity.getRuc()),
                entity.getPlanId(),
                TenantStatus.fromString(entity.getStatus()),
                BrandColor.of(entity.getBrandColor()),
                entity.getCreatedAt()
        );
    }

    public static TenantJpaEntity toEntity(Tenant domain) {
        if (domain == null) return null;
        return new TenantJpaEntity(
                domain.getId().getValue(),
                domain.getCommercialName(),
                domain.getRuc().getValue(),
                domain.getPlanId(),
                domain.getStatus().name(),
                domain.getBrandColor().getValue(),
                domain.getCreatedAt()
        );
    }
}
