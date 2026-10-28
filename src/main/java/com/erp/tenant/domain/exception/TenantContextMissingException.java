package com.erp.tenant.domain.exception;

/**
 * Thrown when an operation requiring an active tenant context is executed
 * without a valid tenant set.
 */
public class TenantContextMissingException extends DomainException {

    public TenantContextMissingException() {
        super("TENANT_CONTEXT_MISSING",
                "La operacion requiere un contexto de taller establecido y no lo tiene.");
    }

    public TenantContextMissingException(String message) {
        super("TENANT_CONTEXT_MISSING", message);
    }
}
