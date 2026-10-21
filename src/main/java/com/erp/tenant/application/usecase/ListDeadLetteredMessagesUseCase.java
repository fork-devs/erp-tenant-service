package com.erp.tenant.application.usecase;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.util.List;
import java.util.UUID;

/**
 * Consulta de los mensajes de la bandeja de salida que agotaron sus
 * intentos. Ya no se reintentan solos, pero se conservan: son la evidencia de
 * un evento que el sistema afirma haber producido y que nunca llego al bus.
 */
public interface ListDeadLetteredMessagesUseCase {

    List<TenantOutboxMessage> list(UUID tenantId, int limit);
}
