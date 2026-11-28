package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.BranchOperatingHourJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataBranchOperatingHourRepository extends JpaRepository<BranchOperatingHourJpaEntity, UUID> {
    List<BranchOperatingHourJpaEntity> findByBranchIdOrderByDayOfWeekAsc(UUID branchId);
    void deleteByBranchId(UUID branchId);
}
