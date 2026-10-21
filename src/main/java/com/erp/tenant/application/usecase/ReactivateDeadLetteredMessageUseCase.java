package com.erp.tenant.application.usecase;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.util.UUID;

/**
 * Devuelve un mensaje agotado a la cola de pendientes, con el contador de
 * intentos restablecido, para que vuelva a intentarse.
 */
public interface ReactivateDeadLetteredMessageUseCase {

    TenantOutboxMessage reactivate(UUID id);
}
