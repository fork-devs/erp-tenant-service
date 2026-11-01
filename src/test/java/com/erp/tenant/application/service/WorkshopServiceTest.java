package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.UpdateWorkshopCommand;
import com.erp.tenant.application.dto.result.WorkshopResult;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.valueobject.BrandColor;
import com.erp.tenant.domain.valueobject.Ruc;
import com.erp.tenant.domain.valueobject.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkshopServiceTest {

    @Mock
    private TenantRepositoryPort tenantRepositoryPort;

    @Mock
    private UnitOfWork unitOfWork;

    private WorkshopService workshopService;

    @BeforeEach
    void setUp() {
        workshopService = new WorkshopService(tenantRepositoryPort, unitOfWork);
    }

    @Test
    @DisplayName("Should update workshop business name and mileage config")
    void shouldUpdateWorkshop() {
        UUID workshopId = UUID.randomUUID();
        Tenant tenant = Tenant.create("Taller Antiguo", Ruc.of("20123456789"), UUID.randomUUID(), BrandColor.defaultColor());

        when(tenantRepositoryPort.findById(new TenantId(workshopId))).thenReturn(Optional.of(tenant));
        when(unitOfWork.callInTransaction(any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        when(tenantRepositoryPort.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateWorkshopCommand command = new UpdateWorkshopCommand(
                workshopId, "Taller Nuevo S.A.C.", "Taller Pro", "20123456789", 7500
        );
        WorkshopResult result = workshopService.update(command);

        assertThat(result.businessName()).isEqualTo("Taller Pro");
        assertThat(result.mileageIntervalConfig()).isEqualTo(7500);
    }

    @Test
    @DisplayName("Should list workshops by owner")
    void shouldListWorkshopsByOwner() {
        UUID ownerId = UUID.randomUUID();
        Tenant tenant = Tenant.create("Taller 1", Ruc.of("20123456789"), UUID.randomUUID(), BrandColor.defaultColor());
        tenant.setOwnerId(ownerId);

        when(tenantRepositoryPort.findByOwnerId(ownerId)).thenReturn(List.of(tenant));

        List<WorkshopResult> results = workshopService.listByOwner(ownerId);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).ownerId()).isEqualTo(ownerId);
    }
}
