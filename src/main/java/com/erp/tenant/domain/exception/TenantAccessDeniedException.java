package com.erp.tenant.domain.exception;

/**
 * Thrown when an authenticated principal attempts to access or mutate a tenant
 * outside their assigned workshop without global platform scope.
 */
public class TenantAccessDeniedException extends DomainException {

    public TenantAccessDeniedException(java.util.UUID tenantDelPrincipal, java.util.UUID tallerSolicitado) {
        super("TENANT_ACCESS_DENIED",
                "El taller " + tallerSolicitado + " no es accesible desde el taller " + tenantDelPrincipal + ".");
    }
}
