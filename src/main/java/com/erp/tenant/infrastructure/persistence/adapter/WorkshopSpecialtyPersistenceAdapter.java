package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.WorkshopSpecialty;
import com.erp.tenant.domain.port.WorkshopSpecialtyRepositoryPort;
import com.erp.tenant.infrastructure.persistence.TransactionGuard;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopSpecialtyJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.WorkshopSpecialtyPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopSpecialtyRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkshopSpecialtyPersistenceAdapter implements WorkshopSpecialtyRepositoryPort {

    private final SpringDataWorkshopSpecialtyRepository repository;
    private final TransactionGuard transactionGuard;

    public WorkshopSpecialtyPersistenceAdapter(SpringDataWorkshopSpecialtyRepository repository,
                                              TransactionGuard transactionGuard) {
        this.repository = repository;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public WorkshopSpecialty save(WorkshopSpecialty specialty) {
        transactionGuard.requireActiveTransaction("saveWorkshopSpecialty");
        WorkshopSpecialtyJpaEntity entity = WorkshopSpecialtyPersistenceMapper.toEntity(specialty);
        WorkshopSpecialtyJpaEntity saved = repository.save(entity);
        return WorkshopSpecialtyPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<WorkshopSpecialty> saveAll(List<WorkshopSpecialty> specialties) {
        transactionGuard.requireActiveTransaction("saveAllWorkshopSpecialties");
        List<WorkshopSpecialtyJpaEntity> entities = specialties.stream()
                .map(WorkshopSpecialtyPersistenceMapper::toEntity)
                .toList();
        return repository.saveAll(entities).stream()
                .map(WorkshopSpecialtyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkshopSpecialty> findById(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(WorkshopSpecialtyPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkshopSpecialty> findByWorkshopId(UUID tenantId, UUID workshopId, boolean activeOnly) {
        List<WorkshopSpecialtyJpaEntity> entities = activeOnly
                ? repository.findByWorkshopIdAndTenantIdAndActiveTrue(workshopId, tenantId)
                : repository.findByWorkshopIdAndTenantId(workshopId, tenantId);
        return entities.stream()
                .map(WorkshopSpecialtyPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkshopSpecialty> findByCode(UUID tenantId, UUID workshopId, String code) {
        return repository.findByWorkshopIdAndTenantIdAndCode(workshopId, tenantId, code)
                .map(WorkshopSpecialtyPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(UUID tenantId, UUID workshopId, String code) {
        return repository.existsByWorkshopIdAndTenantIdAndCode(workshopId, tenantId, code);
    }
}
