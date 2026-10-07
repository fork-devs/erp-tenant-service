package com.erp.tenant.domain.event;

import java.time.Instant;
import java.util.UUID;

public class TenantPlanChangedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID tenantId;
    private final UUID oldPlanId;
    private final UUID newPlanId;
    private final String newPlanCode;
    private final int newMaxUsers;
    private final int newMaxVehicles;
    private final int newMaxIotMsgPerDay;
    private final Instant occurredAt;

    public TenantPlanChangedEvent(
            UUID tenantId,
            UUID oldPlanId,
            UUID newPlanId,
            String newPlanCode,
            int newMaxUsers,
            int newMaxVehicles,
            int newMaxIotMsgPerDay) {
        this.eventId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.oldPlanId = oldPlanId;
        this.newPlanId = newPlanId;
        this.newPlanCode = newPlanCode;
        this.newMaxUsers = newMaxUsers;
        this.newMaxVehicles = newMaxVehicles;
        this.newMaxIotMsgPerDay = newMaxIotMsgPerDay;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "TenantPlanChanged";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getOldPlanId() {
        return oldPlanId;
    }

    public UUID getNewPlanId() {
        return newPlanId;
    }

    public String getNewPlanCode() {
        return newPlanCode;
    }

    public int getNewMaxUsers() {
        return newMaxUsers;
    }

    public int getNewMaxVehicles() {
        return newMaxVehicles;
    }

    public int getNewMaxIotMsgPerDay() {
        return newMaxIotMsgPerDay;
    }
}
