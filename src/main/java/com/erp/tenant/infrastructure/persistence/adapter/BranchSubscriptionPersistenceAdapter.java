package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.BranchSubscription;
import com.erp.tenant.domain.port.BranchSubscriptionRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.BranchSubscriptionJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.BranchSubscriptionPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchSubscriptionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BranchSubscriptionPersistenceAdapter implements BranchSubscriptionRepositoryPort {

    private final SpringDataBranchSubscriptionRepository repository;
    private final TransactionGuard transactionGuard;

    public BranchSubscriptionPersistenceAdapter(SpringDataBranchSubscriptionRepository repository,
                                                TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public BranchSubscription save(BranchSubscription subscription) {
        transactionGuard.requireActiveTransaction("saveBranchSubscription");
        BranchSubscriptionJpaEntity entity = BranchSubscriptionPersistenceMapper.toEntity(subscription);
        BranchSubscriptionJpaEntity saved = repository.save(entity);
        return BranchSubscriptionPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BranchSubscription> findActiveByBranchId(UUID branchId) {
        return repository.findByBranchIdAndStatus(branchId, "ACTIVE")
                .map(BranchSubscriptionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchSubscription> findByBranchId(UUID branchId) {
        return repository.findByBranchId(branchId).stream()
                .map(BranchSubscriptionPersistenceMapper::toDomain)
                .toList();
    }
}
