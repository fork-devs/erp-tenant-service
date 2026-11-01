package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.EmployeeProfile;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeProfileRepositoryPort {
    EmployeeProfile save(EmployeeProfile employee);
    Optional<EmployeeProfile> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<EmployeeProfile> findById(UUID id);
    Optional<EmployeeProfile> findByUserId(UUID userId);
    Optional<EmployeeProfile> findByDocumentNumber(String documentNumber);
    boolean existsByTenantIdAndDocumentNumber(UUID tenantId, String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    void delete(UUID id);
}
