package com.erp.tenant.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TenantProvisionedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID tenantId;
    private final String commercialName;
    private final String ruc;
    private final String planCode;
    private final int maxUsers;
    private final int maxVehicles;
    private final int maxIotMsgPerDay;
    private final Instant occurredAt;

    public TenantProvisionedEvent(
            UUID tenantId,
            String commercialName,
            String ruc,
            String planCode,
            int maxUsers,
            int maxVehicles,
            int maxIotMsgPerDay) {
        this.eventId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.commercialName = commercialName;
        this.ruc = ruc;
        this.planCode = planCode;
        this.maxUsers = maxUsers;
        this.maxVehicles = maxVehicles;
        this.maxIotMsgPerDay = maxIotMsgPerDay;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "TenantProvisioned";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public String getRuc() {
        return ruc;
    }

    public String getPlanCode() {
        return planCode;
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
}
