package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.PlanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPlanRepository extends JpaRepository<PlanJpaEntity, UUID> {
    Optional<PlanJpaEntity> findByCode(String code);
}
