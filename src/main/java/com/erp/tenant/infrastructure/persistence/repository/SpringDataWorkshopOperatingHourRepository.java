package com.erp.tenant.infrastructure.persistence.repository;

import com.erp.tenant.infrastructure.persistence.entity.WorkshopOperatingHourJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataWorkshopOperatingHourRepository extends JpaRepository<WorkshopOperatingHourJpaEntity, UUID> {
    List<WorkshopOperatingHourJpaEntity> findByWorkshopIdOrderByDayOfWeekAsc(UUID workshopId);
    void deleteByWorkshopId(UUID workshopId);
}
