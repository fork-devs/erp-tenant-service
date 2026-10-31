package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataBranchRepository extends JpaRepository<BranchJpaEntity, UUID> {
    Optional<BranchJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    List<BranchJpaEntity> findByWorkshopId(UUID workshopId);
    List<BranchJpaEntity> findByWorkshopIdAndTenantId(UUID workshopId, UUID tenantId);
    boolean existsByWorkshopIdAndCodeIgnoreCase(UUID workshopId, String code);
    Optional<BranchJpaEntity> findByWorkshopIdAndCodeIgnoreCase(UUID workshopId, String code);
}
