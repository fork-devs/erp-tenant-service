package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.ChangePlanCommand;
import com.erp.tenant.application.dto.command.UpdateBrandingCommand;
import com.erp.tenant.application.dto.result.TenantLimitsResult;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.usecase.ChangeTenantPlanUseCase;
import com.erp.tenant.application.usecase.GetTenantDetailsUseCase;
import com.erp.tenant.application.usecase.ReactivateTenantUseCase;
import com.erp.tenant.application.usecase.SuspendTenantUseCase;
import com.erp.tenant.domain.event.TenantPlanChangedEvent;
import com.erp.tenant.domain.event.TenantSuspendedEvent;
import com.erp.tenant.domain.exception.PlanNotFoundException;
import com.erp.tenant.domain.exception.TenantNotFoundException;
import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.TenantId;

import com.erp.tenant.application.usecase.UpdateBrandingUseCase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TenantManagementService implements
        ChangeTenantPlanUseCase,
        SuspendTenantUseCase,
        ReactivateTenantUseCase,
        UpdateBrandingUseCase,
        GetTenantDetailsUseCase {

    private final TenantRepositoryPort tenantRepositoryPort;
    private final PlanRepositoryPort planRepositoryPort;
    private final TenantOutboxRepositoryPort outboxRepositoryPort;

    public TenantManagementService(
            TenantRepositoryPort tenantRepositoryPort,
            PlanRepositoryPort planRepositoryPort,
            TenantOutboxRepositoryPort outboxRepositoryPort) {
        this.tenantRepositoryPort = tenantRepositoryPort;
        this.planRepositoryPort = planRepositoryPort;
        this.outboxRepositoryPort = outboxRepositoryPort;
    }

    @Override
    public TenantResult changePlan(ChangePlanCommand command) {
        if (command.tenantId() == null) {
            throw new IllegalArgumentException("TenantId es obligatorio");
        }

        TenantId tenantId = new TenantId(command.tenantId());
        Tenant tenant = tenantRepositoryPort.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(command.tenantId().toString()));

        UUID oldPlanId = tenant.getPlanId();
        Plan newPlan = resolveNewPlan(command);

        tenant.changePlan(newPlan.getId());
        Tenant updatedTenant = tenantRepositoryPort.save(tenant);

        // Publicar evento en Outbox
        TenantPlanChangedEvent event = new TenantPlanChangedEvent(
                updatedTenant.getId().getValue(),
                oldPlanId,
                newPlan.getId(),
                newPlan.getCode().name(),
                newPlan.getMaxUsers(),
                newPlan.getMaxVehicles(),
                newPlan.getMaxIotMsgPerDay()
        );

        String payload = String.format(
                "{\"eventId\":\"%s\",\"eventType\":\"%s\",\"tenantId\":\"%s\",\"oldPlanId\":\"%s\",\"newPlanId\":\"%s\",\"newPlanCode\":\"%s\",\"maxUsers\":%d,\"maxVehicles\":%d,\"maxIotMsgPerDay\":%d,\"occurredAt\":\"%s\"}",
                event.getEventId(),
                event.getEventType(),
                event.getTenantId(),
                event.getOldPlanId(),
                event.getNewPlanId(),
                event.getNewPlanCode(),
                event.getNewMaxUsers(),
                event.getNewMaxVehicles(),
                event.getNewMaxIotMsgPerDay(),
                event.getOccurredAt()
        );

        outboxRepositoryPort.save(TenantOutboxMessage.of(updatedTenant.getId().getValue(), event.getEventType(), payload));

        return toResult(updatedTenant, newPlan);
    }

    @Override
    public TenantResult suspendTenant(UUID tenantId, String reason) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId es obligatorio");
        }

        Tenant tenant = tenantRepositoryPort.findById(new TenantId(tenantId))
                .orElseThrow(() -> new TenantNotFoundException(tenantId.toString()));

        tenant.suspend();
        Tenant savedTenant = tenantRepositoryPort.save(tenant);

        TenantSuspendedEvent event = new TenantSuspendedEvent(tenantId, savedTenant.getRuc().getValue(), reason != null ? reason : "Suspensión administrativa");
        String payload = String.format(
                "{\"eventId\":\"%s\",\"eventType\":\"%s\",\"tenantId\":\"%s\",\"ruc\":\"%s\",\"reason\":\"%s\",\"occurredAt\":\"%s\"}",
                event.getEventId(), event.getEventType(), event.getTenantId(), event.getRuc(), event.getReason().replace("\"", "\\\""), event.getOccurredAt()
        );

        outboxRepositoryPort.save(TenantOutboxMessage.of(tenantId, event.getEventType(), payload));

        Plan plan = planRepositoryPort.findById(savedTenant.getPlanId()).orElse(null);
        return toResult(savedTenant, plan);
    }

    @Override
    public TenantResult reactivateTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId es obligatorio");
        }

        Tenant tenant = tenantRepositoryPort.findById(new TenantId(tenantId))
                .orElseThrow(() -> new TenantNotFoundException(tenantId.toString()));

        tenant.reactivate();
        Tenant savedTenant = tenantRepositoryPort.save(tenant);

        Plan plan = planRepositoryPort.findById(savedTenant.getPlanId()).orElse(null);
        return toResult(savedTenant, plan);
    }

    public TenantResult updateBranding(UpdateBrandingCommand command) {
        if (command.tenantId() == null) {
            throw new IllegalArgumentException("TenantId es obligatorio");
        }

        Tenant tenant = tenantRepositoryPort.findById(new TenantId(command.tenantId()))
                .orElseThrow(() -> new TenantNotFoundException(command.tenantId().toString()));

        BrandColor color = command.brandColor() != null ? BrandColor.of(command.brandColor()) : null;
        tenant.updateBranding(command.commercialName(), color);
        Tenant savedTenant = tenantRepositoryPort.save(tenant);

        Plan plan = planRepositoryPort.findById(savedTenant.getPlanId()).orElse(null);
        return toResult(savedTenant, plan);
    }

    @Override
    public TenantResult getById(UUID tenantId) {
        Tenant tenant = tenantRepositoryPort.findById(new TenantId(tenantId))
                .orElseThrow(() -> new TenantNotFoundException(tenantId.toString()));
        Plan plan = planRepositoryPort.findById(tenant.getPlanId()).orElse(null);
        return toResult(tenant, plan);
    }

    @Override
    public TenantLimitsResult getLimitsById(UUID tenantId) {
        Tenant tenant = tenantRepositoryPort.findById(new TenantId(tenantId))
                .orElseThrow(() -> new TenantNotFoundException(tenantId.toString()));

        Plan plan = planRepositoryPort.findById(tenant.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException(tenant.getPlanId().toString()));

        return new TenantLimitsResult(
                tenant.getId().getValue(),
                tenant.getStatus().name(),
                plan.getCode().name(),
                plan.getMaxUsers(),
                plan.getMaxVehicles(),
                plan.getMaxIotMsgPerDay()
        );
    }

    @Override
    public List<TenantResult> listAll() {
        return tenantRepositoryPort.findAll().stream()
                .map(t -> {
                    Plan plan = planRepositoryPort.findById(t.getPlanId()).orElse(null);
                    return toResult(t, plan);
                })
                .collect(Collectors.toList());
    }

    private Plan resolveNewPlan(ChangePlanCommand command) {
        if (command.newPlanId() != null) {
            return planRepositoryPort.findById(command.newPlanId())
                    .orElseThrow(() -> new PlanNotFoundException(command.newPlanId().toString()));
        }
        if (command.newPlanCode() != null && !command.newPlanCode().trim().isEmpty()) {
            PlanCode planCode = PlanCode.fromString(command.newPlanCode());
            return planRepositoryPort.findByCode(planCode)
                    .orElseThrow(() -> new PlanNotFoundException(planCode.name()));
        }
        throw new IllegalArgumentException("Debe especificar newPlanId o newPlanCode");
    }

    private TenantResult toResult(Tenant tenant, Plan plan) {
        String planCodeName = plan != null ? plan.getCode().name() : "UNKNOWN";
        return new TenantResult(
                tenant.getId().getValue(),
                tenant.getCommercialName(),
                tenant.getRuc().getValue(),
                tenant.getPlanId(),
                planCodeName,
                tenant.getStatus().name(),
                tenant.getBrandColor().getValue(),
                tenant.getCreatedAt()
        );
    }
}
