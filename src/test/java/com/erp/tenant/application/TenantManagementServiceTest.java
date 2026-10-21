package com.erp.tenant.application;

import com.erp.tenant.application.dto.command.ChangePlanCommand;
import com.erp.tenant.application.dto.command.UpdateBrandingCommand;
import com.erp.tenant.application.dto.result.TenantLimitsResult;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantManagementService;
import com.erp.tenant.domain.exception.TenantNotFoundException;
import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantManagementServiceTest {

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Mock
    private PlanRepositoryPort planRepositoryPort;

    @Mock
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    /**
     * UnitOfWork de prueba: ejecuta la operacion en linea. La demarcacion
     * transaccional como tal se verifica en los tests de integracion; aqui lo
     * que importa es que el caso de uso delega la atomicidad en el puerto.
     */
    private static final com.erp.tenant.domain.port.UnitOfWork DIRECTO = new com.erp.tenant.domain.port.UnitOfWork() {
        @Override
        public void runInTransaction(Runnable work) {
            work.run();
        }

        @Override
        public <T> T callInTransaction(java.util.function.Supplier<T> work) {
            return work.get();
        }
    };

    private TenantManagementService managementService;

    private Plan basicPlan;
    private Plan proPlan;
    private Tenant sampleTenant;

    @BeforeEach
    void setUp() {
        managementService = new TenantManagementService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort,
                DIRECTO, new com.erp.tenant.infrastructure.messaging.DomainEventSerializer(new com.fasterxml.jackson.databind.ObjectMapper()));
        basicPlan = new Plan(UUID.randomUUID(), PlanCode.BASICO, 3, 50, 1000, new BigDecimal("99.00"));
        proPlan = new Plan(UUID.randomUUID(), PlanCode.PRO, 10, 200, 5000, new BigDecimal("249.00"));
        sampleTenant = Tenant.create("Taller Test", Ruc.of("20601234567"), basicPlan.getId(), BrandColor.of("#2563EB"));
    }

    @Test
    @DisplayName("Should change tenant plan and persist TenantPlanChanged event to outbox")
    void shouldChangePlanSuccessfully() {
        UUID tenantId = sampleTenant.getId().getValue();
        ChangePlanCommand command = new ChangePlanCommand(tenantId, proPlan.getId(), null);

        when(tenantRepositoryPort.findById(new TenantId(tenantId))).thenReturn(Optional.of(sampleTenant));
        when(planRepositoryPort.findById(proPlan.getId())).thenReturn(Optional.of(proPlan));
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(i -> i.getArgument(0));

        TenantResult result = managementService.changePlan(command);

        assertNotNull(result);
        assertEquals("PRO", result.planCode());
        assertEquals(proPlan.getId(), result.planId());

        // Verify outbox
        ArgumentCaptor<TenantOutboxMessage> outboxCaptor = ArgumentCaptor.forClass(TenantOutboxMessage.class);
        verify(outboxRepositoryPort, times(1)).save(outboxCaptor.capture());

        TenantOutboxMessage message = outboxCaptor.getValue();
        assertEquals("TenantPlanChanged", message.getEventType());
        assertEquals(tenantId, message.getAggregateId());
        assertTrue(message.getPayload().contains("PRO"));
    }

    @Test
    @DisplayName("Should suspend tenant and persist TenantSuspended event to outbox")
    void shouldSuspendTenantSuccessfully() {
        UUID tenantId = sampleTenant.getId().getValue();

        when(tenantRepositoryPort.findById(new TenantId(tenantId))).thenReturn(Optional.of(sampleTenant));
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepositoryPort.findById(sampleTenant.getPlanId())).thenReturn(Optional.of(basicPlan));

        TenantResult result = managementService.suspendTenant(tenantId, "Falta de pago");

        assertNotNull(result);
        assertEquals("SUSPENDED", result.status());

        ArgumentCaptor<TenantOutboxMessage> outboxCaptor = ArgumentCaptor.forClass(TenantOutboxMessage.class);
        verify(outboxRepositoryPort, times(1)).save(outboxCaptor.capture());

        TenantOutboxMessage message = outboxCaptor.getValue();
        assertEquals("TenantSuspended", message.getEventType());
        assertTrue(message.getPayload().contains("Falta de pago"));
    }

    @Test
    @DisplayName("Should reactivate a suspended tenant")
    void shouldReactivateTenantSuccessfully() {
        sampleTenant.suspend();
        UUID tenantId = sampleTenant.getId().getValue();

        when(tenantRepositoryPort.findById(new TenantId(tenantId))).thenReturn(Optional.of(sampleTenant));
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepositoryPort.findById(sampleTenant.getPlanId())).thenReturn(Optional.of(basicPlan));

        TenantResult result = managementService.reactivateTenant(tenantId);

        assertNotNull(result);
        assertEquals("ACTIVE", result.status());
    }

    @Test
    @DisplayName("Should update branding correctly")
    void shouldUpdateBrandingSuccessfully() {
        UUID tenantId = sampleTenant.getId().getValue();
        UpdateBrandingCommand command = new UpdateBrandingCommand(tenantId, "Nuevo Nombre Taller", "#FF5733");

        when(tenantRepositoryPort.findById(new TenantId(tenantId))).thenReturn(Optional.of(sampleTenant));
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepositoryPort.findById(sampleTenant.getPlanId())).thenReturn(Optional.of(basicPlan));

        TenantResult result = managementService.updateBranding(command);

        assertEquals("Nuevo Nombre Taller", result.commercialName());
        assertEquals("#FF5733", result.brandColor());
    }

    @Test
    @DisplayName("Should get tenant limits for gateway and rate limiter")
    void shouldGetTenantLimits() {
        UUID tenantId = sampleTenant.getId().getValue();

        when(tenantRepositoryPort.findById(new TenantId(tenantId))).thenReturn(Optional.of(sampleTenant));
        when(planRepositoryPort.findById(sampleTenant.getPlanId())).thenReturn(Optional.of(basicPlan));

        TenantLimitsResult limits = managementService.getLimitsById(tenantId);

        assertNotNull(limits);
        assertEquals(tenantId, limits.tenantId());
        assertEquals("ACTIVE", limits.status());
        assertEquals("BASICO", limits.planCode());
        assertEquals(3, limits.maxUsers());
        assertEquals(50, limits.maxVehicles());
        assertEquals(1000, limits.maxIotMsgPerDay());
    }

    @Test
    @DisplayName("Should throw TenantNotFoundException when tenant does not exist")
    void shouldThrowWhenTenantNotFound() {
        UUID randomId = UUID.randomUUID();
        when(tenantRepositoryPort.findById(new TenantId(randomId))).thenReturn(Optional.empty());

        assertThrows(TenantNotFoundException.class, () -> managementService.getById(randomId));
    }
}
