package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyRevisionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWorkshopSpecialtyRevisionRepository extends JpaRepository<WorkshopSpecialtyRevisionJpaEntity, UUID> {

    List<WorkshopSpecialtyRevisionJpaEntity> findBySpecialtyIdAndTenantIdOrderByChangedAtAsc(UUID specialtyId, UUID tenantId);
}
