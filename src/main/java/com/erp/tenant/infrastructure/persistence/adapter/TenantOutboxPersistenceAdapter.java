package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.TenantOutboxPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataTenantOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TenantOutboxPersistenceAdapter implements TenantOutboxRepositoryPort {

    private final SpringDataTenantOutboxRepository repository;

    public TenantOutboxPersistenceAdapter(SpringDataTenantOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TenantOutboxMessage save(TenantOutboxMessage message) {
        TenantOutboxJpaEntity entity = TenantOutboxPersistenceMapper.toEntity(message);
        TenantOutboxJpaEntity saved = repository.save(entity);
        return TenantOutboxPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantOutboxMessage> findUnpublished(int limit) {
        return repository.findUnpublished(PageRequest.of(0, limit)).stream()
                .map(TenantOutboxPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsPublished(UUID id) {
        repository.markAsPublished(id);
    }
}
