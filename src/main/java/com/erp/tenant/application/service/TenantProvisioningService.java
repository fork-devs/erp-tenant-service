package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.logging.SecurityEventLogger;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.usecase.ProvisionTenantUseCase;
import com.erp.tenant.domain.event.TenantProvisionedEvent;
import com.erp.tenant.domain.exception.PlanNotFoundException;
import com.erp.tenant.domain.exception.RucAlreadyExistsException;
import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.infrastructure.messaging.DomainEventSerializer;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;

import java.util.Optional;

public class TenantProvisioningService implements ProvisionTenantUseCase {

    private final TenantRepositoryPort tenantRepositoryPort;
    private final PlanRepositoryPort planRepositoryPort;
    private final TenantOutboxRepositoryPort outboxRepositoryPort;
    private final UnitOfWork unitOfWork;
    private final DomainEventSerializer eventSerializer;

    public TenantProvisioningService(
            TenantRepositoryPort tenantRepositoryPort,
            PlanRepositoryPort planRepositoryPort,
            TenantOutboxRepositoryPort outboxRepositoryPort,
            UnitOfWork unitOfWork,
            DomainEventSerializer eventSerializer) {
        this.tenantRepositoryPort = tenantRepositoryPort;
        this.planRepositoryPort = planRepositoryPort;
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.unitOfWork = unitOfWork;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public TenantResult provisionTenant(ProvisionTenantCommand command) {
        if (command.commercialName() == null || command.commercialName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre comercial del taller es obligatorio");
        }
        if (command.ruc() == null || command.ruc().trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC del taller es obligatorio");
        }

        Ruc ruc = Ruc.of(command.ruc());

        // El taller y el evento de dominio se confirman juntos: si la
        // escritura del evento falla, el taller tampoco se crea. Sin este
        // limite, cada llamada a un repositorio abria su propia transaccion y
        // un fallo intermedio dejaba un taller sin su evento, que es un
        // estado que nadie puede recuperar.
        return unitOfWork.callInTransaction(() -> {
            if (tenantRepositoryPort.existsByRuc(ruc)) {
                throw new RucAlreadyExistsException(ruc.getValue());
            }

            // Resolver Plan
            Plan plan = resolvePlan(command);

            BrandColor brandColor = BrandColor.of(command.brandColor());
            Tenant tenant = Tenant.create(command.commercialName(), ruc, plan.getId(), brandColor);

            // 1. Guardar Tenant en PostgreSQL
            Tenant savedTenant = tenantRepositoryPort.save(tenant);

            // 2. Crear Evento de Dominio TenantProvisioned
            TenantProvisionedEvent event = new TenantProvisionedEvent(
                    savedTenant.getId().getValue(),
                    savedTenant.getCommercialName(),
                    savedTenant.getRuc().getValue(),
                    plan.getCode().name(),
                    plan.getMaxUsers(),
                    plan.getMaxVehicles(),
                    plan.getMaxIotMsgPerDay()
            );

            String payloadJson = eventSerializer.serialize(event);

            // 3. Guardar en la Transactional Outbox, en la MISMA transaccion
            TenantOutboxMessage outboxMessage = TenantOutboxMessage.of(
                    savedTenant.getId().getValue(),
                    savedTenant.getId().getValue(),
                    event.getEventId(),
                    event.getEventType(),
                    payloadJson
            );
            outboxRepositoryPort.save(outboxMessage);

            SecurityEventLogger.registrar("taller_creado", savedTenant.getId().getValue(), null, null, "ok",
                    java.util.Map.of());

            return new TenantResult(
                    savedTenant.getId().getValue(),
                    savedTenant.getCommercialName(),
                    savedTenant.getRuc().getValue(),
                    plan.getId(),
                    plan.getCode().name(),
                    savedTenant.getStatus().name(),
                    savedTenant.getBrandColor().getValue(),
                    savedTenant.getCreatedAt()
            );
        });
    }

    private Plan resolvePlan(ProvisionTenantCommand command) {
        if (command.planId() != null) {
            return planRepositoryPort.findById(command.planId())
                    .orElseThrow(() -> new PlanNotFoundException(command.planId().toString()));
        }
        if (command.planCode() != null && !command.planCode().trim().isEmpty()) {
            PlanCode planCode = PlanCode.fromString(command.planCode());
            return planRepositoryPort.findByCode(planCode)
                    .orElseThrow(() -> new PlanNotFoundException(planCode.name()));
        }
        // Plan por defecto
        return planRepositoryPort.findByCode(PlanCode.BASICO)
                .orElseThrow(() -> new PlanNotFoundException("BASICO"));
    }


}
