package com.erp.tenant.domain.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Segregated Anti-Corruption Layer (ACL) port for querying and updating customer profiles in erp-fleet-service.
 */
public interface CustomerProfileQueryPort {

    record CustomerProfileSnapshot(
            UUID customerId,
            UUID userId,
            String firstName,
            String lastName,
            String documentType,
            String documentNumber,
            String phone,
            String profileImageUrl
    ) {}

    Optional<CustomerProfileSnapshot> findByUserId(UUID userId);

    Optional<CustomerProfileSnapshot> findByDocumentNumber(String documentNumber);

    Optional<CustomerProfileSnapshot> updateProfileImage(UUID userId, String profileImageUrl);
}
