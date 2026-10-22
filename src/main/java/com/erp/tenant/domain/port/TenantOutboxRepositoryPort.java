package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.TenantOutboxMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TenantOutboxRepositoryPort {

    TenantOutboxMessage save(TenantOutboxMessage message);

    /**
     * Mensajes pendientes de publicar, cuya fecha de proximo intento ya ha
     * vencido. La reclamacion es exclusiva: dos llamadas concurrentes
     * obtienen conjuntos disjuntos.
     */
    List<TenantOutboxMessage> findPending(Instant now, int limit);

    /**
     * Mensajes agotados, que ya no se reintentan solos pero se conservan.
     */
    List<TenantOutboxMessage> findDeadLettered(UUID tenantId, int limit);

    /**
     * Devuelve un mensaje agotado a la cola de pendientes, con el contador de
     * intentos restablecido.
     */
    TenantOutboxMessage reactivate(UUID id);
}
