package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.command.CreateOwnerProfileCommand;
import com.erp.tenant.application.dto.command.UpdateOwnerProfileCommand;
import com.erp.tenant.application.dto.result.OwnerProfileResult;

import java.util.UUID;

public interface ManageOwnerProfileUseCase {
    OwnerProfileResult create(CreateOwnerProfileCommand command);
    OwnerProfileResult update(UpdateOwnerProfileCommand command);
    void delete(UUID ownerId);
}
