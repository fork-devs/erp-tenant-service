package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.UpdateWorkshopCommand;
import com.erp.tenant.application.dto.result.WorkshopResult;

import java.util.List;
import java.util.UUID;

public interface ManageWorkshopUseCase {
    WorkshopResult update(UpdateWorkshopCommand command);
    WorkshopResult getById(UUID workshopId);
    List<WorkshopResult> listByOwner(UUID ownerId);
}
