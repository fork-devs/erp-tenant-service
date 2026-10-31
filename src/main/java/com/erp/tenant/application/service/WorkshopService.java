package com.erp.tenant.application.service;

import com.erp.tenant.application.dto.command.UpdateWorkshopCommand;
import com.erp.tenant.application.dto.result.WorkshopResult;
import com.erp.tenant.application.usecase.ManageWorkshopUseCase;
import com.erp.tenant.domain.exception.TenantNotFoundException;
import com.erp.tenant.domain.model.Tenant;
import com.erp.tenant.domain.port.TenantRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import com.erp.tenant.domain.valueobject.TenantId;

import java.util.List;
import java.util.UUID;

public class WorkshopService implements ManageWorkshopUseCase {

    private final TenantRepositoryPort tenantRepositoryPort;
    private final UnitOfWork unitOfWork;

    public WorkshopService(TenantRepositoryPort tenantRepositoryPort,
                           UnitOfWork unitOfWork) {
        this.tenantRepositoryPort = tenantRepositoryPort;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public WorkshopResult update(UpdateWorkshopCommand command) {
        if (command == null || command.workshopId() == null) {
            throw new IllegalArgumentException("Workshop ID is required for update");
        }
        TenantId tenantId = new TenantId(command.workshopId());
        Tenant tenant = tenantRepositoryPort.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Workshop not found with ID: " + command.workshopId()));

        String newName = command.brandName() != null ? command.brandName() : command.businessName();
        tenant.updateWorkshop(newName, command.mileageIntervalConfig());

        Tenant saved = unitOfWork.callInTransaction(() -> tenantRepositoryPort.save(tenant));
        return WorkshopResult.from(saved);
    }

    @Override
    public WorkshopResult getById(UUID workshopId) {
        return tenantRepositoryPort.findById(new TenantId(workshopId))
                .map(WorkshopResult::from)
                .orElseThrow(() -> new TenantNotFoundException("Workshop not found with ID: " + workshopId));
    }

    @Override
    public List<WorkshopResult> listByOwner(UUID ownerId) {
        return tenantRepositoryPort.findByOwnerId(ownerId).stream()
                .map(WorkshopResult::from)
                .toList();
    }
}
