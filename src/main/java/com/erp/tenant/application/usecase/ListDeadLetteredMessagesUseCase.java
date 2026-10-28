package com.erp.tenant.application.usecase;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.util.List;
import java.util.UUID;

/**
 * Use case for listing Dead Letter Queue (DLQ) outbox messages that exceeded max retry attempts.
 */
public interface ListDeadLetteredMessagesUseCase {

    List<TenantOutboxMessage> list(UUID tenantId, int limit);
}
