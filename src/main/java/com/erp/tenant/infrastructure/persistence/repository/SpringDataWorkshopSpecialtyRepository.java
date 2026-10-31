package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWorkshopSpecialtyRepository extends JpaRepository<WorkshopSpecialtyJpaEntity, UUID> {

    Optional<WorkshopSpecialtyJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<WorkshopSpecialtyJpaEntity> findByWorkshopIdAndTenantId(UUID workshopId, UUID tenantId);

    List<WorkshopSpecialtyJpaEntity> findByWorkshopIdAndTenantIdAndActiveTrue(UUID workshopId, UUID tenantId);

    Optional<WorkshopSpecialtyJpaEntity> findByWorkshopIdAndTenantIdAndCode(UUID workshopId, UUID tenantId, String code);

    boolean existsByWorkshopIdAndTenantIdAndCode(UUID workshopId, UUID tenantId, String code);
}
