package com.erp.tenant.application.usecase;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.util.UUID;

/**
 * Use case for reactivating an exhausted DLQ outbox message back to pending status.
 */
public interface ReactivateDeadLetteredMessageUseCase {

    TenantOutboxMessage reactivate(UUID id);
}
