package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.EmployeeProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEmployeeProfileRepository extends JpaRepository<EmployeeProfileJpaEntity, UUID> {
    Optional<EmployeeProfileJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<EmployeeProfileJpaEntity> findByUserId(UUID userId);
    Optional<EmployeeProfileJpaEntity> findByDocumentNumber(String documentNumber);
    boolean existsByTenantIdAndDocumentNumber(UUID tenantId, String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
}
