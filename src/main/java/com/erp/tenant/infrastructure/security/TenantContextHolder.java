package com.erp.tenant.infrastructure.security;

import java.util.UUID;

/**
 * Thread-local context holder for the active tenant during request execution.
 */
public final class TenantContextHolder {

    private static final InheritableThreadLocal<UUID> CURRENT_TENANT = new InheritableThreadLocal<>();

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
