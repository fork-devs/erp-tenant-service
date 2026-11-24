package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopRoleAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataWorkshopRoleAuditLogRepository extends JpaRepository<WorkshopRoleAuditLogJpaEntity, UUID> {
    List<WorkshopRoleAuditLogJpaEntity> findByWorkshopIdOrderByRecordedAtDesc(UUID workshopId);
}
