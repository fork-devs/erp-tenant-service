package com.erp.tenant.domain.model;

public enum PlanCode {
    BASICO,
    PRO,
    ENTERPRISE;

    public static PlanCode fromString(String code) {
        if (code == null) {
            throw new IllegalArgumentException("El código de plan no puede ser nulo");
        }
        String normalized = code.trim().toUpperCase();
        if ("BASIC".equals(normalized)) {
            return BASICO;
        }
        return PlanCode.valueOf(normalized);
    }
}
