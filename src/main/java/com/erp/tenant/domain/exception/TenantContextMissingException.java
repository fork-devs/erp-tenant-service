package com.erp.tenant.domain.exception;

/**
 * Se lanza cuando una operacion que requiere un taller de contexto se
 * ejecuta sin contexto establecido.
 *
 * <p>Ver docs/doc-estado-implementacion.md, hallazgo P0 5.4.
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
