package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.TenantOutboxPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataTenantOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TenantOutboxPersistenceAdapter implements TenantOutboxRepositoryPort {

    private final TransactionGuard transactionGuard;
    private final SpringDataTenantOutboxRepository repository;

    public TenantOutboxPersistenceAdapter(SpringDataTenantOutboxRepository repository,
                                        TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public TenantOutboxMessage save(TenantOutboxMessage message) {
        transactionGuard.requireActiveTransaction("saveOutboxMessage");
        TenantOutboxJpaEntity entity = TenantOutboxPersistenceMapper.toEntity(message);
        return TenantOutboxPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public List<TenantOutboxMessage> findPending(Instant now, int limit) {
        return repository.findPendingForUpdate(now, PageRequest.of(0, limit)).stream()
                .map(TenantOutboxPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantOutboxMessage> findDeadLettered(UUID tenantId, int limit) {
        return repository.findDeadLettered(tenantId, PageRequest.of(0, limit)).stream()
                .map(TenantOutboxPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public TenantOutboxMessage reactivate(UUID id) {
        TenantOutboxJpaEntity entity = repository.findByIdForUpdate(id);
        if (entity == null) {
            return null;
        }
        TenantOutboxMessage message = TenantOutboxPersistenceMapper.toDomain(entity);
        message.reactivate();
        return TenantOutboxPersistenceMapper.toDomain(repository.save(TenantOutboxPersistenceMapper.toEntity(message)));
    }
}
