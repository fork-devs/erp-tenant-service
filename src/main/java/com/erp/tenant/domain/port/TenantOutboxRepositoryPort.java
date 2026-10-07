package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.util.List;
import java.util.UUID;

public interface TenantOutboxRepositoryPort {
    TenantOutboxMessage save(TenantOutboxMessage message);
    List<TenantOutboxMessage> findUnpublished(int limit);
    void markAsPublished(UUID id);
}
