package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.Branch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepositoryPort {
    Branch save(Branch branch);
    Optional<Branch> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Branch> findById(UUID id);
    List<Branch> findByWorkshopId(UUID workshopId);
    List<Branch> findByWorkshopIdAndTenantId(UUID workshopId, UUID tenantId);
    boolean existsByWorkshopIdAndCode(UUID workshopId, String code);
    Optional<Branch> findByWorkshopIdAndCode(UUID workshopId, String code);
}
