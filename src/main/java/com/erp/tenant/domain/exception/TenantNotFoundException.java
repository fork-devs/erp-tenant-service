package com.erp.tenant.domain.exception;

public class TenantNotFoundException extends DomainException {
    public TenantNotFoundException(String identifier) {
        super("TENANT_NOT_FOUND", "No se encontró el taller automotriz: " + identifier);
    }
}
