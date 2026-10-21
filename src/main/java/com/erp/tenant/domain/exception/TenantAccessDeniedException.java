package com.erp.tenant.domain.exception;

/**
 * Se lanza cuando un principal intenta operar sobre un taller distinto del
 * suyo y no tiene alcance transversal.
 *
 * <p>Ver docs/doc-estado-implementacion.md, hallazgo P0 5.4.
 */
public class TenantAccessDeniedException extends DomainException {

    public TenantAccessDeniedException(java.util.UUID tenantDelPrincipal, java.util.UUID tallerSolicitado) {
        super("TENANT_ACCESS_DENIED",
                "El taller " + tallerSolicitado + " no es accesible desde el taller " + tenantDelPrincipal + ".");
    }
}
