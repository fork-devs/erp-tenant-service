package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAssignmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataWorkshopRoleAssignmentRepository extends JpaRepository<WorkshopRoleAssignmentJpaEntity, UUID> {
    boolean existsByWorkshopIdAndWorkshopRoleIdAndUserId(UUID workshopId, UUID workshopRoleId, UUID userId);
    List<WorkshopRoleAssignmentJpaEntity> findByWorkshopIdAndUserId(UUID workshopId, UUID userId);
}
