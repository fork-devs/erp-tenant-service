package com.erp.tenant.domain.model;

import com.erp.tenant.domain.exception.TenantSuspendedException;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Tenant implements Serializable {
    private final TenantId id;
    private String commercialName;
    private final Ruc ruc;
    private UUID planId;
    private TenantStatus status;
    private BrandColor brandColor;
    private UUID ownerId;
    private Integer mileageIntervalConfig;
    private final Instant createdAt;

    public Tenant(TenantId id,
                  String commercialName,
                  Ruc ruc,
                  UUID planId,
                  TenantStatus status,
                  BrandColor brandColor,
                  Instant createdAt) {
        this(id, commercialName, ruc, planId, status, brandColor, null, 5000, createdAt);
    }

    public Tenant(TenantId id,
                  String commercialName,
                  Ruc ruc,
                  UUID planId,
                  TenantStatus status,
                  BrandColor brandColor,
                  UUID ownerId,
                  Integer mileageIntervalConfig,
                  Instant createdAt) {
        if (id == null || commercialName == null || ruc == null || planId == null) {
            throw new IllegalArgumentException("Campos obligatorios del Tenant no pueden ser nulos");
        }
        if (commercialName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre comercial del taller no puede estar vacío");
        }
        this.id = id;
        this.commercialName = commercialName.trim();
        this.ruc = ruc;
        this.planId = planId;
        this.status = status != null ? status : TenantStatus.ACTIVE;
        this.brandColor = brandColor != null ? brandColor : BrandColor.defaultColor();
        this.ownerId = ownerId;
        this.mileageIntervalConfig = mileageIntervalConfig != null ? mileageIntervalConfig : 5000;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Tenant create(String commercialName, Ruc ruc, UUID planId, BrandColor brandColor) {
        return new Tenant(
                TenantId.generate(),
                commercialName,
                ruc,
                planId,
                TenantStatus.ACTIVE,
                brandColor,
                Instant.now()
        );
    }

    public void changePlan(UUID newPlanId) {
        if (newPlanId == null) {
            throw new IllegalArgumentException("El nuevo plan no puede ser nulo");
        }
        if (this.status == TenantStatus.SUSPENDED) {
            throw new TenantSuspendedException("No se puede cambiar el plan a un taller suspendido");
        }
        this.planId = newPlanId;
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public void reactivate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void updateBranding(String newCommercialName, BrandColor newBrandColor) {
        if (newCommercialName != null && !newCommercialName.trim().isEmpty()) {
            this.commercialName = newCommercialName.trim();
        }
        if (newBrandColor != null) {
            this.brandColor = newBrandColor;
        }
    }

    public boolean isActive() {
        return this.status == TenantStatus.ACTIVE;
    }

    public TenantId getId() {
        return id;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public Ruc getRuc() {
        return ruc;
    }

    public UUID getPlanId() {
        return planId;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getMileageIntervalConfig() {
        return mileageIntervalConfig;
    }

    public void setMileageIntervalConfig(Integer mileageIntervalConfig) {
        this.mileageIntervalConfig = mileageIntervalConfig != null ? mileageIntervalConfig : 5000;
    }

    public void updateWorkshop(String commercialName, Integer mileageIntervalConfig) {
        if (commercialName != null && !commercialName.trim().isEmpty()) {
            this.commercialName = commercialName.trim();
        }
        if (mileageIntervalConfig != null) {
            this.mileageIntervalConfig = mileageIntervalConfig;
        }
    }

    public BrandColor getBrandColor() {
        return brandColor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tenant tenant)) return false;
        return Objects.equals(id, tenant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
