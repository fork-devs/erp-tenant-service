package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a subscription plan assigned to a branch.
 */
public class BranchSubscription implements Serializable {

    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID planId;
    private final String billingCycle;
    private String status;
    private final Instant startDate;
    private Instant endDate;
    private final Instant createdAt;

    public BranchSubscription(UUID id,
                              UUID tenantId,
                              UUID branchId,
                              UUID planId,
                              String billingCycle,
                              String status,
                              Instant startDate,
                              Instant endDate,
                              Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("Subscription ID is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("Branch ID is required");
        }
        if (planId == null) {
            throw new IllegalArgumentException("Plan ID is required");
        }
        if (billingCycle == null || (!billingCycle.equalsIgnoreCase("MONTHLY") && !billingCycle.equalsIgnoreCase("ANNUAL"))) {
            throw new IllegalArgumentException("Billing cycle must be MONTHLY or ANNUAL");
        }

        this.id = id;
        this.tenantId = tenantId;
        this.branchId = branchId;
        this.planId = planId;
        this.billingCycle = billingCycle.toUpperCase();
        this.status = status != null ? status.toUpperCase() : "ACTIVE";
        this.startDate = startDate != null ? startDate : Instant.now();
        this.endDate = endDate != null ? endDate : calculateEndDate(this.startDate, this.billingCycle);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static BranchSubscription create(UUID tenantId, UUID branchId, UUID planId, String billingCycle) {
        Instant now = Instant.now();
        String cycle = billingCycle != null ? billingCycle.toUpperCase() : "MONTHLY";
        Instant end = calculateEndDate(now, cycle);
        return new BranchSubscription(
                UUID.randomUUID(),
                tenantId,
                branchId,
                planId,
                cycle,
                "ACTIVE",
                now,
                end,
                now
        );
    }

    private static Instant calculateEndDate(Instant start, String cycle) {
        if ("ANNUAL".equalsIgnoreCase(cycle)) {
            return start.plus(365, ChronoUnit.DAYS);
        }
        return start.plus(30, ChronoUnit.DAYS);
    }

    public void cancel() {
        this.status = "CANCELED";
        this.endDate = Instant.now();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) && Instant.now().isBefore(this.endDate);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchSubscription that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
