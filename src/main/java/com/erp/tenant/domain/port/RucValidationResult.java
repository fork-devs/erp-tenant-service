package com.erp.tenant.domain.port;

public record RucValidationResult(
        String ruc,
        String legalName,
        String tradeName,
        String status,
        String condition,
        String fiscalAddress,
        boolean valid
) {
    public boolean isActiveAndHabido() {
        return valid
                && "ACTIVO".equalsIgnoreCase(status)
                && "HABIDO".equalsIgnoreCase(condition);
    }

    public static RucValidationResult invalid(String ruc) {
        return new RucValidationResult(ruc, null, null, null, null, null, false);
    }
}
