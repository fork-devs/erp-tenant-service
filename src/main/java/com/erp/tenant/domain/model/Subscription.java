package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Subscription implements Serializable {
    private final UUID id;
    private final UUID tenantId;
    private UUID planId;
    private String stripeSubscriptionId;
    private String status;
    private Instant currentPeriodEnd;
    private final Instant createdAt;

    public Subscription(UUID id, UUID tenantId, UUID planId, String stripeSubscriptionId, String status, Instant currentPeriodEnd, Instant createdAt) {
        if (id == null || tenantId == null || planId == null) {
            throw new IllegalArgumentException("Campos obligatorios de Subscription no pueden ser nulos");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.planId = planId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.status = status != null ? status : "ACTIVE";
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Subscription create(UUID tenantId, UUID planId, String stripeSubscriptionId, Instant currentPeriodEnd) {
        return new Subscription(UUID.randomUUID(), tenantId, planId, stripeSubscriptionId, "ACTIVE", currentPeriodEnd, Instant.now());
    }

    public void updatePlan(UUID newPlanId, Instant newPeriodEnd) {
        this.planId = newPlanId;
        this.currentPeriodEnd = newPeriodEnd;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
