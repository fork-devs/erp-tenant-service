package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TenantOutboxRepositoryPort {

    TenantOutboxMessage save(TenantOutboxMessage message);

    /**
     * Fetches pending outbox messages ready for delivery. Claiming uses row locking
     * so concurrent workers receive disjoint partitions.
     */
    List<TenantOutboxMessage> findPending(Instant now, int limit);

    /**
     * Fetches exhausted messages in the Dead Letter Queue.
     */
    List<TenantOutboxMessage> findDeadLettered(UUID tenantId, int limit);

    /**
     * Reactivates an exhausted DLQ message back to PENDING status with reset attempt counter.
     */
    TenantOutboxMessage reactivate(UUID id);
}
