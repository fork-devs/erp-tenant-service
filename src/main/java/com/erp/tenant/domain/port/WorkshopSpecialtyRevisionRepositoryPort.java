package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.WorkshopSpecialtyRevision;

import java.util.List;
import java.util.UUID;

public interface WorkshopSpecialtyRevisionRepositoryPort {

    WorkshopSpecialtyRevision save(WorkshopSpecialtyRevision revision);

    List<WorkshopSpecialtyRevision> saveAll(List<WorkshopSpecialtyRevision> revisions);

    List<WorkshopSpecialtyRevision> findBySpecialtyId(UUID tenantId, UUID specialtyId);
}
