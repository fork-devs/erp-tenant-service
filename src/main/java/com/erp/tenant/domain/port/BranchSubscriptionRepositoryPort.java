package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.BranchSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchSubscriptionRepositoryPort {
    BranchSubscription save(BranchSubscription subscription);
    Optional<BranchSubscription> findActiveByBranchId(UUID branchId);
    List<BranchSubscription> findByBranchId(UUID branchId);
}
