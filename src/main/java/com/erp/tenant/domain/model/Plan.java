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
    private final BigDecimal priceMonthly;

    public Plan(UUID id, PlanCode code, int maxUsers, int maxVehicles, int maxIotMsgPerDay, BigDecimal priceMonthly) {
        if (id == null || code == null || priceMonthly == null) {
            throw new IllegalArgumentException("Campos requeridos de Plan no pueden ser nulos");
        }
        this.id = id;
        this.code = code;
        this.maxUsers = maxUsers;
        this.maxVehicles = maxVehicles;
        this.maxIotMsgPerDay = maxIotMsgPerDay;
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
