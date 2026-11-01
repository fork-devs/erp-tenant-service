package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.OwnerProfile;

import java.util.Optional;
import java.util.UUID;

public interface OwnerProfileRepositoryPort {
    OwnerProfile save(OwnerProfile owner);
    Optional<OwnerProfile> findById(UUID id);
    Optional<OwnerProfile> findByUserId(UUID userId);
    Optional<OwnerProfile> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByUserId(UUID userId);
    void delete(UUID id);
}
