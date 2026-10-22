package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Plan implements Serializable {
    private final UUID id;
    private final PlanCode code;
    private final int maxUsers;
    private final int maxVehicles;
    private final int maxIotMsgPerDay;
    private final int maxRequestsPerMinute;
    private final int maxConcurrentSlots;
    private final BigDecimal priceMonthly;

    public Plan(UUID id, PlanCode code, int maxUsers, int maxVehicles, int maxIotMsgPerDay,
                int maxRequestsPerMinute, int maxConcurrentSlots, BigDecimal priceMonthly) {
        if (id == null || code == null || priceMonthly == null) {
            throw new IllegalArgumentException("Campos requeridos de Plan no pueden ser nulos");
        }
        if (maxRequestsPerMinute <= 0 || maxConcurrentSlots <= 0) {
            // Un plan sin limites de trafico no es un plan valido: el gateway
            // los aplica como cuota, y un limite de cero o negativo lo
            // dejaria sin poder trabajar (ADR-007).
            throw new IllegalArgumentException("Los limites de trafico del Plan deben ser positivos");
        }
        this.id = id;
        this.code = code;
        this.maxUsers = maxUsers;
        this.maxVehicles = maxVehicles;
        this.maxIotMsgPerDay = maxIotMsgPerDay;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.maxConcurrentSlots = maxConcurrentSlots;
        this.priceMonthly = priceMonthly;
    }

    public UUID getId() {
        return id;
    }

    public PlanCode getCode() {
        return code;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public int getMaxIotMsgPerDay() {
        return maxIotMsgPerDay;
    }

    /**
     * Peticiones por minuto que el plan permite. Lo aplica el gateway como
     * cuota de trafico (ADR-007).
     */
    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    /**
     * Conexiones concurrentes que el plan permite. Lo aplica el gateway como
     * mamparo por taller (ADR-007).
     */
    public int getMaxConcurrentSlots() {
        return maxConcurrentSlots;
    }

    public BigDecimal getPriceMonthly() {
        return priceMonthly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan plan)) return false;
        return Objects.equals(id, plan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
