package com.erp.tenant.domain.model;

public enum PlanCode {
    BASIC,
    PRO,
    ENTERPRISE;

    public static PlanCode fromString(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Plan code cannot be null");
        }
        return PlanCode.valueOf(code.trim().toUpperCase());
    }
}
