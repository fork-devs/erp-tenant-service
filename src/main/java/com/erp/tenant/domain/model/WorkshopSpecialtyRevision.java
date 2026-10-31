package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkshopSpecialtyRevision implements Serializable {

    private final UUID id;
    private final UUID tenantId;
    private final UUID specialtyId;
    private final String fieldName;
    private final String oldValue;
    private final String newValue;
    private final String changedBy;
    private final Instant changedAt;

    public WorkshopSpecialtyRevision(UUID id,
                                    UUID tenantId,
                                    UUID specialtyId,
                                    String fieldName,
                                    String oldValue,
                                    String newValue,
                                    String changedBy,
                                    Instant changedAt) {
        if (id == null || tenantId == null || specialtyId == null || fieldName == null) {
            throw new IllegalArgumentException("Campos obligatorios de la revisión no pueden ser nulos");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.specialtyId = specialtyId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy != null ? changedBy : "SYSTEM";
        this.changedAt = changedAt != null ? changedAt : Instant.now();
    }

    public static WorkshopSpecialtyRevision recordChange(UUID tenantId,
                                                         UUID specialtyId,
                                                         String fieldName,
                                                         String oldValue,
                                                         String newValue,
                                                         String changedBy) {
        return new WorkshopSpecialtyRevision(
                UUID.randomUUID(),
                tenantId,
                specialtyId,
                fieldName,
                oldValue,
                newValue,
                changedBy,
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getSpecialtyId() {
        return specialtyId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkshopSpecialtyRevision that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
