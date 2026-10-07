package com.erp.tenant.domain.exception;

public class RucAlreadyExistsException extends DomainException {
    public RucAlreadyExistsException(String ruc) {
        super("TENANT_RUC_EXISTS", "Ya existe un taller registrado con el RUC: " + ruc);
    }
}
