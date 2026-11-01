package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;
import com.erp.tenant.domain.port.WorkshopSpecialtyRevisionRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyRevisionJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.WorkshopSpecialtyRevisionPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopSpecialtyRevisionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class WorkshopSpecialtyRevisionPersistenceAdapter implements WorkshopSpecialtyRevisionRepositoryPort {

    private final SpringDataWorkshopSpecialtyRevisionRepository repository;
    private final TransactionGuard transactionGuard;

    public WorkshopSpecialtyRevisionPersistenceAdapter(SpringDataWorkshopSpecialtyRevisionRepository repository,
                                                      TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public WorkshopSpecialtyRevision save(WorkshopSpecialtyRevision revision) {
        transactionGuard.requireActiveTransaction("saveWorkshopSpecialtyRevision");
        WorkshopSpecialtyRevisionJpaEntity entity = WorkshopSpecialtyRevisionPersistenceMapper.toEntity(revision);
        WorkshopSpecialtyRevisionJpaEntity saved = repository.save(entity);
        return WorkshopSpecialtyRevisionPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<WorkshopSpecialtyRevision> saveAll(List<WorkshopSpecialtyRevision> revisions) {
        transactionGuard.requireActiveTransaction("saveAllWorkshopSpecialtyRevisions");
        List<WorkshopSpecialtyRevisionJpaEntity> entities = revisions.stream()
                .map(WorkshopSpecialtyRevisionPersistenceMapper::toEntity)
                .toList();
        return repository.saveAll(entities).stream()
                .map(WorkshopSpecialtyRevisionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkshopSpecialtyRevision> findBySpecialtyId(UUID tenantId, UUID specialtyId) {
        return repository.findBySpecialtyIdAndTenantIdOrderByChangedAtAsc(specialtyId, tenantId).stream()
                .map(WorkshopSpecialtyRevisionPersistenceMapper::toDomain)
                .toList();
    }
}
