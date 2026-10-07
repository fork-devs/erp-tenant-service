package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantJpaEntity {

    @Id
    private UUID id;

    @Column(name = "commercial_name", nullable = false, length = 255)
    private String commercialName;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "brand_color", length = 10)
    private String brandColor;

    @Column(name = "created_at")
    private Instant createdAt;

    public TenantJpaEntity() {}

    public TenantJpaEntity(UUID id, String commercialName, String ruc, UUID planId, String status, String brandColor, Instant createdAt) {
        this.id = id;
        this.commercialName = commercialName;
        this.ruc = ruc;
        this.planId = planId;
        this.status = status;
        this.brandColor = brandColor;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrandColor() {
        return brandColor;
    }

    public void setBrandColor(String brandColor) {
        this.brandColor = brandColor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
