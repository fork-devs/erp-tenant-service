package com.erp.tenant.application.usecase;

import com.erp.tenant.application.dto.result.SpecialtyResult;
import com.erp.tenant.application.dto.result.SpecialtyRevisionResult;

import java.util.List;
import java.util.UUID;

public interface GetWorkshopSpecialtiesUseCase {

    List<SpecialtyResult> list(UUID tenantId, UUID workshopId, boolean activeOnly);

    SpecialtyResult getById(UUID tenantId, UUID workshopId, UUID specialtyId);

    List<SpecialtyRevisionResult> getHistory(UUID tenantId, UUID workshopId, UUID specialtyId);
}
