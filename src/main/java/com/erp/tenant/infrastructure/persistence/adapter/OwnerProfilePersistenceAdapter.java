package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.OwnerProfile;
import com.erp.tenant.domain.port.OwnerProfileRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.OwnerProfilePersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOwnerProfileRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class OwnerProfilePersistenceAdapter implements OwnerProfileRepositoryPort {

    private final SpringDataOwnerProfileRepository repository;
    private final TransactionGuard transactionGuard;

    public OwnerProfilePersistenceAdapter(SpringDataOwnerProfileRepository repository,
                                         TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public OwnerProfile save(OwnerProfile owner) {
        transactionGuard.requireActiveTransaction("saveOwnerProfile");
        OwnerProfileJpaEntity entity = OwnerProfilePersistenceMapper.toEntity(owner);
        OwnerProfileJpaEntity saved = repository.save(entity);
        return OwnerProfilePersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OwnerProfile> findById(UUID id) {
        return repository.findById(id)
                .map(OwnerProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OwnerProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .map(OwnerProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OwnerProfile> findByDocumentNumber(String documentNumber) {
        return repository.findByDocumentNumber(documentNumber)
                .map(OwnerProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public void delete(UUID id) {
        transactionGuard.requireActiveTransaction("deleteOwnerProfile");
        repository.deleteById(id);
    }
}
