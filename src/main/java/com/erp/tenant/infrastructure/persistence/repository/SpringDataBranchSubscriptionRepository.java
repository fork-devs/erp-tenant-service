package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.BranchSubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataBranchSubscriptionRepository extends JpaRepository<BranchSubscriptionJpaEntity, UUID> {
    Optional<BranchSubscriptionJpaEntity> findByBranchIdAndStatus(UUID branchId, String status);
    List<BranchSubscriptionJpaEntity> findByBranchId(UUID branchId);
}
