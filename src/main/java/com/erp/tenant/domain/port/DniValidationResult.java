package com.erp.tenant.domain.port;

public record DniValidationResult(
        String documentNumber,
        String firstName,
        String paternalSurname,
        String maternalSurname,
        String fullName,
        boolean valid
) {
    public static DniValidationResult invalid(String documentNumber) {
        return new DniValidationResult(documentNumber, null, null, null, null, false);
    }
}
