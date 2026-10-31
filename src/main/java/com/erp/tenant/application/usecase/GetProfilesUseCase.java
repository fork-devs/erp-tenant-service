package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.ProfileUnifiedResult;

import java.util.List;
import java.util.UUID;

public interface GetProfilesUseCase {
    List<String> getRolesByUserId(UUID userId);
    ProfileUnifiedResult getByDocumentNumber(String documentNumber);
}
