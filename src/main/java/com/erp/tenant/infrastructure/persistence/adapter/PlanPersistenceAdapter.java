package com.erp.tenant.infrastructure.persistence.adapter;

import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.infrastructure.persistence.mapper.PlanPersistenceMapper;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataPlanRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PlanPersistenceAdapter implements PlanRepositoryPort {

    private final SpringDataPlanRepository springDataPlanRepository;

    public PlanPersistenceAdapter(SpringDataPlanRepository springDataPlanRepository) {
        this.springDataPlanRepository = springDataPlanRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> findById(UUID id) {
        return springDataPlanRepository.findById(id)
                .map(PlanPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> findByCode(PlanCode code) {
        return springDataPlanRepository.findByCode(code.name())
                .map(PlanPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> findAll() {
        return springDataPlanRepository.findAll().stream()
                .map(PlanPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
