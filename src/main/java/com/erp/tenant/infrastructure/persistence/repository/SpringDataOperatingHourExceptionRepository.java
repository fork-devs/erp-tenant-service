package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.OperatingHourExceptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOperatingHourExceptionRepository extends JpaRepository<OperatingHourExceptionJpaEntity, UUID> {
    List<OperatingHourExceptionJpaEntity> findByScopeTypeAndScopeIdOrderByExceptionDateAsc(String scopeType, UUID scopeId);
    Optional<OperatingHourExceptionJpaEntity> findByScopeTypeAndScopeIdAndExceptionDate(String scopeType, UUID scopeId, LocalDate exceptionDate);
    void deleteByIdAndScopeTypeAndScopeId(UUID id, String scopeType, UUID scopeId);
}
