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
            throw new IllegalArgumentException("Required fields of Plan cannot be null");
        }
        if (maxRequestsPerMinute <= 0 || maxConcurrentSlots <= 0) {
            // A plan without traffic limits is invalid: the gateway enforces them
            // as quotas; a zero or negative limit would render the tenant unusable (ADR-007).
            throw new IllegalArgumentException("Traffic limits of Plan must be positive");
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
     * Requests per minute permitted by the plan. Enforced by the gateway as a rate quota (ADR-007).
     */
    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    /**
     * Concurrent connections permitted by the plan. Enforced by the gateway as a per-tenant bulkhead (ADR-007).
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
