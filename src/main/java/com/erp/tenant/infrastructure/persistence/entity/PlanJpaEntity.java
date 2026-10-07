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

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    public PlanJpaEntity() {}

    public PlanJpaEntity(UUID id, String code, Integer maxUsers, Integer maxVehicles, Integer maxIotMsgPerDay, BigDecimal priceMonthly) {
        this.id = id;
        this.code = code;
        this.maxUsers = maxUsers;
        this.maxVehicles = maxVehicles;
        this.maxIotMsgPerDay = maxIotMsgPerDay;
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
}
