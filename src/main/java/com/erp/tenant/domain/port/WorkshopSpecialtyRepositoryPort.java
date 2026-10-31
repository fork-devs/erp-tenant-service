package com.erp.tenant.domain.port;

import com.erp.tenant.domain.model.WorkshopSpecialty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkshopSpecialtyRepositoryPort {

    WorkshopSpecialty save(WorkshopSpecialty specialty);

    List<WorkshopSpecialty> saveAll(List<WorkshopSpecialty> specialties);

    Optional<WorkshopSpecialty> findById(UUID tenantId, UUID id);

    List<WorkshopSpecialty> findByWorkshopId(UUID tenantId, UUID workshopId, boolean activeOnly);

    Optional<WorkshopSpecialty> findByCode(UUID tenantId, UUID workshopId, String code);

    boolean existsByCode(UUID tenantId, UUID workshopId, String code);
}
