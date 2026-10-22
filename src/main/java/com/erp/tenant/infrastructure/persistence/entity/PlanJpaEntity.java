package com.erp.tenant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class PlanJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_vehicles", nullable = false)
    private Integer maxVehicles;

    @Column(name = "max_iot_msg_per_day", nullable = false)
    private Integer maxIotMsgPerDay;

    /**
     * Limites de TRAFICO del plan: peticiones por minuto y conexiones
     * concurrentes. Son parte del plan (ADR-007) y los aplica el gateway.
     */
    @Column(name = "max_requests_per_minute", nullable = false)
    private Integer maxRequestsPerMinute;

    @Column(name = "max_concurrent_slots", nullable = false)
    private Integer maxConcurrentSlots;

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    public PlanJpaEntity() {}

    public PlanJpaEntity(UUID id, String code, Integer maxUsers, Integer maxVehicles, Integer maxIotMsgPerDay,
                          Integer maxRequestsPerMinute, Integer maxConcurrentSlots, BigDecimal priceMonthly) {
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

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxVehicles() {
        return maxVehicles;
    }

    public void setMaxVehicles(Integer maxVehicles) {
        this.maxVehicles = maxVehicles;
    }

    public Integer getMaxIotMsgPerDay() {
        return maxIotMsgPerDay;
    }

    public void setMaxIotMsgPerDay(Integer maxIotMsgPerDay) {
        this.maxIotMsgPerDay = maxIotMsgPerDay;
    }

    public BigDecimal getPriceMonthly() {
        return priceMonthly;
    }

    public void setPriceMonthly(BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    public Integer getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public void setMaxRequestsPerMinute(Integer maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    public Integer getMaxConcurrentSlots() {
        return maxConcurrentSlots;
    }

    public void setMaxConcurrentSlots(Integer maxConcurrentSlots) {
        this.maxConcurrentSlots = maxConcurrentSlots;
    }
}
