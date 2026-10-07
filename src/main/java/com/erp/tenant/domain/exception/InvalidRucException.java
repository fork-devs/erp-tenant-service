package com.erp.tenant.domain.exception;

public class InvalidRucException extends DomainException {
    public InvalidRucException(String message) {
        super("TENANT_INVALID_RUC", message);
    }
}
