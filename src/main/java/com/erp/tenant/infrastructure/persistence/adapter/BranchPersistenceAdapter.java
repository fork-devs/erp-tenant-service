package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.Branch;
import com.erp.tenant.domain.port.BranchRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.BranchPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BranchPersistenceAdapter implements BranchRepositoryPort {

    private final SpringDataBranchRepository repository;
    private final TransactionGuard transactionGuard;

    public BranchPersistenceAdapter(SpringDataBranchRepository repository,
                                    TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public Branch save(Branch branch) {
        transactionGuard.requireActiveTransaction("saveBranch");
        BranchJpaEntity entity = BranchPersistenceMapper.toEntity(branch);
        BranchJpaEntity saved = repository.save(entity);
        return BranchPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(BranchPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> findById(UUID id) {
        return repository.findById(id)
                .map(BranchPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> findByWorkshopId(UUID workshopId) {
        return repository.findByWorkshopId(workshopId).stream()
                .map(BranchPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> findByWorkshopIdAndTenantId(UUID workshopId, UUID tenantId) {
        return repository.findByWorkshopIdAndTenantId(workshopId, tenantId).stream()
                .map(BranchPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByWorkshopIdAndCode(UUID workshopId, String code) {
        return repository.existsByWorkshopIdAndCodeIgnoreCase(workshopId, code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Branch> findByWorkshopIdAndCode(UUID workshopId, String code) {
        return repository.findByWorkshopIdAndCodeIgnoreCase(workshopId, code)
                .map(BranchPersistenceMapper::toDomain);
    }
}
