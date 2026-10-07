package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepositoryPort {
    Optional<Plan> findById(UUID id);
    Optional<Plan> findByCode(PlanCode code);
    List<Plan> findAll();
}
