package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.Subscription;
import com.erp.tenant.domain.port.SubscriptionRepositoryPort;
import com.erp.tenant.infrastructure.persistence.entity.SubscriptionJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.SubscriptionPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataSubscriptionRepository;
import org.springframework.stereotype.Component;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionPersistenceAdapter implements SubscriptionRepositoryPort {

    private final TransactionGuard transactionGuard;
    private final SpringDataSubscriptionRepository repository;

    public SubscriptionPersistenceAdapter(SpringDataSubscriptionRepository repository,
                                      TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public Subscription save(Subscription subscription) {
        transactionGuard.requireActiveTransaction("saveSubscription");
        SubscriptionJpaEntity entity = SubscriptionPersistenceMapper.toEntity(subscription);
        SubscriptionJpaEntity saved = repository.save(entity);
        return SubscriptionPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId)
                .map(SubscriptionPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return repository.findByStripeSubscriptionId(stripeSubscriptionId)
                .map(SubscriptionPersistenceMapper::toDomain);
    }
}
