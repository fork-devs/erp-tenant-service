package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WorkshopSpecialty implements Serializable {

    private final UUID id;
    private final UUID tenantId;
    private final UUID workshopId;
    private String name;
    private final String code;
    private String description;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public WorkshopSpecialty(UUID id,
                             UUID tenantId,
                             UUID workshopId,
                             String name,
                             String code,
                             String description,
                             boolean active,
                             Instant createdAt,
                             Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la especialidad es obligatorio");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("El tenantId es obligatorio");
        }
        if (workshopId == null) {
            throw new IllegalArgumentException("El workshopId es obligatorio");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la especialidad es obligatorio");
        }
        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.name = name.trim();
        this.code = normalizeCode(code);
        this.description = description != null ? description.trim() : null;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static WorkshopSpecialty create(UUID tenantId,
                                          UUID workshopId,
                                          String name,
                                          String code,
                                          String description) {
        Instant now = Instant.now();
        return new WorkshopSpecialty(
                UUID.randomUUID(),
                tenantId,
                workshopId,
                name,
                code,
                description,
                true,
                now,
                now
        );
    }

    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    public void update(String newName, String newDescription) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la especialidad no puede estar vacío");
        }
        this.name = newName.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getWorkshopId() {
        return workshopId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkshopSpecialty that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WorkshopSpecialty{" +
                "id=" + id +
                ", workshopId=" + workshopId +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
