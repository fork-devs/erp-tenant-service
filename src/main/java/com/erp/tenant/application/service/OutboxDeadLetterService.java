package com.erp.tenant.application.service;

import com.erp.tenant.application.usecase.ListDeadLetteredMessagesUseCase;
import com.erp.tenant.application.usecase.ReactivateDeadLetteredMessageUseCase;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;

import java.util.List;
import java.util.UUID;

public class OutboxDeadLetterService implements ListDeadLetteredMessagesUseCase, ReactivateDeadLetteredMessageUseCase {

    private final TenantOutboxRepositoryPort outboxRepositoryPort;
    private final UnitOfWork unitOfWork;

    public OutboxDeadLetterService(TenantOutboxRepositoryPort outboxRepositoryPort, UnitOfWork unitOfWork) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<TenantOutboxMessage> list(UUID tenantId, int limit) {
        return outboxRepositoryPort.findDeadLettered(tenantId, limit);
    }

    @Override
    public TenantOutboxMessage reactivate(UUID id) {
        return unitOfWork.callInTransaction(() -> outboxRepositoryPort.reactivate(id));
    }
}
