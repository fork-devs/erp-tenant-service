package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.domain.model.TenantOutboxStatus;
import com.erp.tenant.infrastructure.persistence.entity.TenantOutboxJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTenantOutboxRepository extends JpaRepository<TenantOutboxJpaEntity, UUID> {

    /**
     * Retrieves pending outbox messages ready for retry using exclusive row locking.
     *
     * <p>Enforces exclusive claiming via {@code FOR UPDATE SKIP LOCKED} so concurrent
     * relay workers obtain disjoint sets without double-publishing messages.
     */
    @Query(value = "SELECT * FROM tenant_outbox "
            + "WHERE status = 'PENDING' AND next_attempt_at <= :now "
            + "ORDER BY next_attempt_at ASC "
            + "FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<TenantOutboxJpaEntity> findPendingForUpdate(@Param("now") Instant now, Pageable pageable);

    @Query(value = "SELECT * FROM tenant_outbox "
            + "WHERE status = 'DEAD_LETTER' AND (:tenantId IS NULL OR tenant_id = :tenantId) "
            + "ORDER BY created_at DESC",
            nativeQuery = true)
    List<TenantOutboxJpaEntity> findDeadLettered(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT o FROM TenantOutboxJpaEntity o WHERE o.id = :id")
    TenantOutboxJpaEntity findByIdForUpdate(@Param("id") UUID id);
}
