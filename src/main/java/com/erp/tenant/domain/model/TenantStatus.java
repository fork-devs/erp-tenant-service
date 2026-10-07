package com.erp.tenant.domain.model;

public enum TenantStatus {
    ACTIVE,
    SUSPENDED;

    public static TenantStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del taller no puede ser nulo");
        }
        return TenantStatus.valueOf(status.trim().toUpperCase());
    }
}
