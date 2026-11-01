package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.CreateSpecialtyCommand;
import com.erp.tenant.application.dto.command.UpdateSpecialtyCommand;
import com.erp.tenant.application.dto.result.SpecialtyResult;

import java.util.UUID;

public interface ManageWorkshopSpecialtyUseCase {

    SpecialtyResult create(CreateSpecialtyCommand command);

    SpecialtyResult update(UpdateSpecialtyCommand command);

    void deactivate(UUID tenantId, UUID workshopId, UUID specialtyId, String actorId);

    void seedBaseSpecialties(UUID tenantId, UUID workshopId);
}
