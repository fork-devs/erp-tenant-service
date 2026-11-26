package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataWorkshopRoleRepository extends JpaRepository<WorkshopRoleJpaEntity, UUID> {
    List<WorkshopRoleJpaEntity> findByWorkshopIdAndActiveTrueOrderByNameAsc(UUID workshopId);
    Optional<WorkshopRoleJpaEntity> findByIdAndWorkshopId(UUID id, UUID workshopId);
    boolean existsByWorkshopIdAndNameIgnoreCase(UUID workshopId, String name);
    boolean existsByWorkshopIdAndTemplateCodeIgnoreCase(UUID workshopId, String templateCode);
}
