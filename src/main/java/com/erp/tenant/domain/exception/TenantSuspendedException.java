package com.erp.tenant.domain.exception;

public class TenantSuspendedException extends DomainException {
    public TenantSuspendedException(String message) {
        super("TENANT_SUSPENDED", message);
    }
}
