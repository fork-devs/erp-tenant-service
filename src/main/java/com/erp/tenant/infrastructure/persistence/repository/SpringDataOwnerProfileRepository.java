package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.OwnerProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataOwnerProfileRepository extends JpaRepository<OwnerProfileJpaEntity, UUID> {
    Optional<OwnerProfileJpaEntity> findByUserId(UUID userId);
    Optional<OwnerProfileJpaEntity> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByUserId(UUID userId);
}
