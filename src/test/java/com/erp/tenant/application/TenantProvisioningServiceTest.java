package com.erp.tenant.application;

import com.erp.tenant.application.dto.command.ProvisionTenantCommand;
import com.erp.tenant.application.dto.result.TenantResult;
import com.erp.tenant.application.service.TenantProvisioningService;
import com.erp.tenant.domain.exception.PlanNotFoundException;
import com.erp.tenant.domain.exception.RucAlreadyExistsException;
import com.erp.tenant.domain.model.Plan;
import com.erp.tenant.domain.model.PlanCode;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.PlanRepositoryPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.valueobject.Ruc;
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
class TenantProvisioningServiceTest {

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Mock
    private PlanRepositoryPort planRepositoryPort;

    @Mock
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    private TenantProvisioningService provisioningService;

    private Plan basicPlan;

    @BeforeEach
    void setUp() {
        provisioningService = new TenantProvisioningService(tenantRepositoryPort, planRepositoryPort, outboxRepositoryPort);
        basicPlan = new Plan(UUID.randomUUID(), PlanCode.BASICO, 3, 50, 1000, new BigDecimal("99.00"));
    }

    @Test
    @DisplayName("Should provision a new tenant and save event into outbox atomically")
    void shouldProvisionTenantSuccessfully() {
        String rucStr = "20601234567";
        ProvisionTenantCommand command = new ProvisionTenantCommand(
                "Taller Mecánico El Veloz",
                rucStr,
                "BASICO",
                null,
                "#2563EB"
        );

        when(tenantRepositoryPort.existsByRuc(Ruc.of(rucStr))).thenReturn(false);
        when(planRepositoryPort.findByCode(PlanCode.BASICO)).thenReturn(Optional.of(basicPlan));
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantResult result = provisioningService.provisionTenant(command);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("Taller Mecánico El Veloz", result.commercialName());
        assertEquals(rucStr, result.ruc());
        assertEquals("BASICO", result.planCode());
        assertEquals("ACTIVE", result.status());

        // Verify tenant was saved
        verify(tenantRepositoryPort, times(1)).save(any(Tenant.class));

        // Verify outbox message was saved
        ArgumentCaptor<TenantOutboxMessage> outboxCaptor = ArgumentCaptor.forClass(TenantOutboxMessage.class);
        verify(outboxRepositoryPort, times(1)).save(outboxCaptor.capture());

        TenantOutboxMessage savedMessage = outboxCaptor.getValue();
        assertEquals("TenantProvisioned", savedMessage.getEventType());
        assertEquals(result.id(), savedMessage.getAggregateId());
        assertFalse(savedMessage.isPublished());
        assertTrue(savedMessage.getPayload().contains(rucStr));
        assertTrue(savedMessage.getPayload().contains("Taller Mecánico El Veloz"));
    }

    @Test
    @DisplayName("Should throw RucAlreadyExistsException if RUC is already registered")
    void shouldThrowWhenRucAlreadyExists() {
        String rucStr = "20601234567";
        ProvisionTenantCommand command = new ProvisionTenantCommand(
                "Taller Duplicado",
                rucStr,
                "BASICO",
                null,
                null
        );

        when(tenantRepositoryPort.existsByRuc(Ruc.of(rucStr))).thenReturn(true);

        assertThrows(RucAlreadyExistsException.class, () -> provisioningService.provisionTenant(command));
        verify(tenantRepositoryPort, never()).save(any());
        verify(outboxRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw PlanNotFoundException when plan code is not found")
    void shouldThrowWhenPlanNotFound() {
        String rucStr = "20601234567";
        ProvisionTenantCommand command = new ProvisionTenantCommand(
                "Taller Nuevo",
                rucStr,
                "ENTERPRISE",
                null,
                null
        );

        when(tenantRepositoryPort.existsByRuc(Ruc.of(rucStr))).thenReturn(false);
        when(planRepositoryPort.findByCode(PlanCode.ENTERPRISE)).thenReturn(Optional.empty());

        assertThrows(PlanNotFoundException.class, () -> provisioningService.provisionTenant(command));
        verify(tenantRepositoryPort, never()).save(any());
        verify(outboxRepositoryPort, never()).save(any());
    }
}
