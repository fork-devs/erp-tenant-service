package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a workshop branch (sede).
 */
public class Branch implements Serializable {

    private final UUID id;
    private final UUID tenantId;
    private final UUID workshopId;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String bannerImageUrl;
    private Double latitude;
    private Double longitude;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public Branch(UUID id,
                  UUID tenantId,
                  UUID workshopId,
                  String code,
                  String name,
                  String address,
                  String phone,
                  boolean active,
                  Instant createdAt,
                  Instant updatedAt) {
        this(id, tenantId, workshopId, code, name, address, phone, null, null, active, createdAt, updatedAt);
    }

    public Branch(UUID id,
                  UUID tenantId,
                  UUID workshopId,
                  String code,
                  String name,
                  String address,
                  String phone,
                  Double latitude,
                  Double longitude,
                  boolean active,
                  Instant createdAt,
                  Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Branch ID is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (workshopId == null) {
            throw new IllegalArgumentException("Workshop ID is required");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch code is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch address is required");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch phone is required");
        }
        validateCoordinates(latitude, longitude);

        this.id = id;
        this.tenantId = tenantId;
        this.workshopId = workshopId;
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.address = address.trim();
        this.phone = phone.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Branch create(UUID tenantId,
                                UUID workshopId,
                                String code,
                                String name,
                                String address,
                                String phone) {
        return create(tenantId, workshopId, code, name, address, phone, null, null);
    }

    public static Branch create(UUID tenantId,
                                UUID workshopId,
                                String code,
                                String name,
                                String address,
                                String phone,
                                Double latitude,
                                Double longitude) {
        Instant now = Instant.now();
        return new Branch(
                UUID.randomUUID(),
                tenantId,
                workshopId,
                code,
                name,
                address,
                phone,
                latitude,
                longitude,
                true,
                now,
                now
        );
    }

    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    public void update(String code, String name, String address, String phone) {
        update(code, name, address, phone, null, null);
    }

    public void update(String code, String name, String address, String phone, Double latitude, Double longitude) {
        if (code != null && !code.trim().isEmpty()) {
            this.code = normalizeCode(code);
        }
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (address != null && !address.trim().isEmpty()) {
            this.address = address.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone.trim();
        }
        if (latitude != null || longitude != null) {
            validateCoordinates(latitude, longitude);
            this.latitude = latitude;
            this.longitude = longitude;
        }
        this.updatedAt = Instant.now();
    }

    private static void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Both latitude and longitude must be provided together");
        }
        if (latitude != null) {
            if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
                throw new IllegalArgumentException("Branch latitude must be between -90 and 90");
            }
            if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
                throw new IllegalArgumentException("Branch longitude must be between -180 and 180");
            }
        }
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = (bannerImageUrl != null && !bannerImageUrl.isBlank()) ? bannerImageUrl.trim() : null;
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
        if (!(o instanceof Branch branch)) return false;
        return Objects.equals(id, branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", workshopId=" + workshopId +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
