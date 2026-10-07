package com.erp.tenant.domain.exception;

public class PlanNotFoundException extends DomainException {
    public PlanNotFoundException(String planIdentifier) {
        super("PLAN_NOT_FOUND", "No se encontró el plan de suscripción: " + planIdentifier);
    }
}
