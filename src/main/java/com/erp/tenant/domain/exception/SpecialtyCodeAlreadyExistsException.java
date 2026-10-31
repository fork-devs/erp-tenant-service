package com.erp.tenant.domain.exception;

public class SpecialtyCodeAlreadyExistsException extends DomainException {
    public SpecialtyCodeAlreadyExistsException(String code) {
        super("SPECIALTY_CODE_EXISTS", "Ya existe una especialidad con el código: " + code);
    }
}
