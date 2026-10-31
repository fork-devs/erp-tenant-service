package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.OwnerProfileResult;

import java.util.UUID;

public interface GetOwnerProfilesUseCase {
    OwnerProfileResult getById(UUID ownerId);
    OwnerProfileResult getByUserId(UUID userId);
}
