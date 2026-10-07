package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;
import com.erp.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.erp.tenant.infrastructure.persistence.mapper.TenantPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataTenantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TenantPersistenceAdapter implements TenantRepositoryPort {

    private final SpringDataTenantRepository springDataTenantRepository;

    public TenantPersistenceAdapter(SpringDataTenantRepository springDataTenantRepository) {
        this.springDataTenantRepository = springDataTenantRepository;
    }

    @Override
    @Transactional
    public Tenant save(Tenant tenant) {
        TenantJpaEntity entity = TenantPersistenceMapper.toEntity(tenant);
        TenantJpaEntity saved = springDataTenantRepository.save(entity);
        return TenantPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(TenantId id) {
        return springDataTenantRepository.findById(id.getValue())
                .map(TenantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findByRuc(Ruc ruc) {
        return springDataTenantRepository.findByRuc(ruc.getValue())
                .map(TenantPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRuc(Ruc ruc) {
        return springDataTenantRepository.existsByRuc(ruc.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return springDataTenantRepository.findAll().stream()
                .map(TenantPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
