package com.erp.tenant.domain.exception;

import java.util.UUID;

public class SpecialtyNotFoundException extends DomainException {
    public SpecialtyNotFoundException(UUID specialtyId) {
        super("SPECIALTY_NOT_FOUND", "No se encontró la especialidad con ID: " + specialtyId);
    }
}
