package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.EmployeeProfile;
import com.erp.tenant.domain.port.EmployeeProfileRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.EmployeeProfileJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.EmployeeProfilePersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataEmployeeProfileRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class EmployeeProfilePersistenceAdapter implements EmployeeProfileRepositoryPort {

    private final SpringDataEmployeeProfileRepository repository;
    private final TransactionGuard transactionGuard;

    public EmployeeProfilePersistenceAdapter(SpringDataEmployeeProfileRepository repository,
                                            TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public EmployeeProfile save(EmployeeProfile employee) {
        transactionGuard.requireActiveTransaction("saveEmployeeProfile");
        EmployeeProfileJpaEntity entity = EmployeeProfilePersistenceMapper.toEntity(employee);
        EmployeeProfileJpaEntity saved = repository.save(entity);
        return EmployeeProfilePersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfile> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(EmployeeProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfile> findById(UUID id) {
        return repository.findById(id)
                .map(EmployeeProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .map(EmployeeProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeProfile> findByDocumentNumber(String documentNumber) {
        return repository.findByDocumentNumber(documentNumber)
                .map(EmployeeProfilePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantIdAndDocumentNumber(UUID tenantId, String documentNumber) {
        return repository.existsByTenantIdAndDocumentNumber(tenantId, documentNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public void delete(UUID id) {
        transactionGuard.requireActiveTransaction("deleteEmployeeProfile");
        repository.deleteById(id);
    }
}
