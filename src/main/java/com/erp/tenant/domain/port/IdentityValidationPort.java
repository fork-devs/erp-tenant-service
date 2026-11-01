package com.erp.tenant.domain.port;

public interface IdentityValidationPort {

    DniValidationResult validateDni(String dni);

    RucValidationResult validateRuc(String ruc);
}
