package com.erp.tenant.infrastructure.security;

import java.util.UUID;

/**
 * Contexto de taller de la peticion en curso.
 *
 * <p>Se establece exclusivamente desde un token verificado y se limpia al
 * terminar la peticion. No existe ningun mecanismo para eludirlo: el
 * aislamiento depende de que el tenant sea el del token.
 *
 * <p>Ver docs/doc-estado-implementacion.md, hallazgo P0 5.4.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
