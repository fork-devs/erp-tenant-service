package com.erp.tenant.application.service;

import com.erp.tenant.domain.exception.BranchNotFoundException;
import com.erp.tenant.domain.model.OperatingHourEntry;
import com.erp.tenant.domain.model.OperatingHourException;
import com.erp.tenant.domain.model.OperatingHoursResolutionStrategy;
import com.erp.tenant.infrastructure.persistence.entity.BranchJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.BranchOperatingHourJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.OperatingHourExceptionJpaEntity;
import com.erp.tenant.infrastructure.persistence.entity.WorkshopOperatingHourJpaEntity;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchOperatingHourRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataBranchRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataOperatingHourExceptionRepository;
import com.erp.tenant.infrastructure.persistence.repository.SpringDataWorkshopOperatingHourRepository;
import com.erp.tenant.infrastructure.security.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OperatingHoursService {

    private static final String SCOPE_WORKSHOP = "WORKSHOP";
    private static final String SCOPE_BRANCH = "BRANCH";

    private final SpringDataWorkshopOperatingHourRepository workshopHourRepo;
    private final SpringDataBranchOperatingHourRepository branchHourRepo;
    private final SpringDataOperatingHourExceptionRepository exceptionRepo;
    private final SpringDataBranchRepository branchRepo;
    private final OperatingHoursResolutionStrategy resolutionStrategy = new OperatingHoursResolutionStrategy();

    public OperatingHoursService(
            SpringDataWorkshopOperatingHourRepository workshopHourRepo,
            SpringDataBranchOperatingHourRepository branchHourRepo,
            SpringDataOperatingHourExceptionRepository exceptionRepo,
            SpringDataBranchRepository branchRepo) {
        this.workshopHourRepo = workshopHourRepo;
        this.branchHourRepo = branchHourRepo;
        this.exceptionRepo = exceptionRepo;
        this.branchRepo = branchRepo;
    }

    @Transactional(readOnly = true)
    public List<OperatingHourEntry> getWorkshopHours(UUID workshopId) {
        return workshopHourRepo.findByWorkshopIdOrderByDayOfWeekAsc(workshopId).stream()
                .map(e -> new OperatingHourEntry(
                        DayOfWeek.of(e.getDayOfWeek()), e.getOpenTime(), e.getCloseTime(), e.isActive()))
                .toList();
    }

    @Transactional
    public List<OperatingHourEntry> putWorkshopHours(UUID workshopId, List<OperatingHourEntry> entries) {
        UUID tenantId = resolveTenantId(workshopId);
        workshopHourRepo.deleteByWorkshopId(workshopId);
        Instant now = Instant.now();
        List<WorkshopOperatingHourJpaEntity> entities = entries.stream()
                .map(e -> new WorkshopOperatingHourJpaEntity(
                        UUID.randomUUID(), tenantId, workshopId,
                        (short) e.dayOfWeek().getValue(),
                        e.openTime() != null ? e.openTime() : LocalTime.MIDNIGHT,
                        e.closeTime() != null ? e.closeTime() : LocalTime.of(23, 59),
                        e.active(), now, now))
                .toList();
        workshopHourRepo.saveAll(entities);
        return entries;
    }

    @Transactional(readOnly = true)
    public List<OperatingHourEntry> getBranchHours(UUID branchId) {
        List<BranchOperatingHourJpaEntity> branchHours = branchHourRepo.findByBranchIdOrderByDayOfWeekAsc(branchId);
        if (!branchHours.isEmpty()) {
            return branchHours.stream()
                    .map(e -> new OperatingHourEntry(
                            DayOfWeek.of(e.getDayOfWeek()), e.getOpenTime(), e.getCloseTime(), e.isActive()))
                    .toList();
        }
        BranchJpaEntity branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
        return getWorkshopHours(branch.getWorkshopId());
    }

    @Transactional
    public List<OperatingHourEntry> putBranchHours(UUID branchId, List<OperatingHourEntry> entries) {
        BranchJpaEntity branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
        UUID tenantId = branch.getTenantId();
        branchHourRepo.deleteByBranchId(branchId);
        Instant now = Instant.now();
        List<BranchOperatingHourJpaEntity> entities = entries.stream()
                .map(e -> new BranchOperatingHourJpaEntity(
                        UUID.randomUUID(), tenantId, branchId,
                        (short) e.dayOfWeek().getValue(),
                        e.openTime() != null ? e.openTime() : LocalTime.MIDNIGHT,
                        e.closeTime() != null ? e.closeTime() : LocalTime.of(23, 59),
                        e.active(), now, now))
                .toList();
        branchHourRepo.saveAll(entities);
        return entries;
    }

    @Transactional
    public void resetBranchHours(UUID branchId) {
        branchRepo.findById(branchId).orElseThrow(() -> new BranchNotFoundException(branchId));
        branchHourRepo.deleteByBranchId(branchId);
    }

    @Transactional(readOnly = true)
    public List<OperatingHourException> listWorkshopExceptions(UUID workshopId) {
        return listExceptions(SCOPE_WORKSHOP, workshopId);
    }

    @Transactional
    public OperatingHourException addWorkshopException(UUID workshopId, LocalDate date, boolean closed,
                                                       LocalTime openTime, LocalTime closeTime, String reason) {
        return addException(resolveTenantId(workshopId), SCOPE_WORKSHOP, workshopId, date, closed, openTime, closeTime, reason);
    }

    @Transactional
    public void deleteWorkshopException(UUID workshopId, UUID exceptionId) {
        exceptionRepo.deleteByIdAndScopeTypeAndScopeId(exceptionId, SCOPE_WORKSHOP, workshopId);
    }

    @Transactional(readOnly = true)
    public List<OperatingHourException> listBranchExceptions(UUID branchId) {
        return listExceptions(SCOPE_BRANCH, branchId);
    }

    @Transactional
    public OperatingHourException addBranchException(UUID branchId, LocalDate date, boolean closed,
                                                     LocalTime openTime, LocalTime closeTime, String reason) {
        BranchJpaEntity branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));
        return addException(branch.getTenantId(), SCOPE_BRANCH, branchId, date, closed, openTime, closeTime, reason);
    }

    @Transactional
    public void deleteBranchException(UUID branchId, UUID exceptionId) {
        exceptionRepo.deleteByIdAndScopeTypeAndScopeId(exceptionId, SCOPE_BRANCH, branchId);
    }

    @Transactional(readOnly = true)
    public OperatingHoursResolutionStrategy.EffectiveAvailability getBranchAvailability(UUID branchId, LocalDate date) {
        BranchJpaEntity branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException(branchId));

        Optional<OperatingHourException> branchEx = exceptionRepo
                .findByScopeTypeAndScopeIdAndExceptionDate(SCOPE_BRANCH, branchId, date)
                .map(this::toDomainException);
        Optional<OperatingHourException> workshopEx = exceptionRepo
                .findByScopeTypeAndScopeIdAndExceptionDate(SCOPE_WORKSHOP, branch.getWorkshopId(), date)
                .map(this::toDomainException);

        List<OperatingHourEntry> branchWeekly = branchHourRepo.findByBranchIdOrderByDayOfWeekAsc(branchId).stream()
                .map(e -> new OperatingHourEntry(DayOfWeek.of(e.getDayOfWeek()), e.getOpenTime(), e.getCloseTime(), e.isActive()))
                .toList();
        List<OperatingHourEntry> workshopWeekly = getWorkshopHours(branch.getWorkshopId());

        return resolutionStrategy.resolve(branchId, date, branchEx, workshopEx, branchWeekly, workshopWeekly);
    }

    private List<OperatingHourException> listExceptions(String scopeType, UUID scopeId) {
        return exceptionRepo.findByScopeTypeAndScopeIdOrderByExceptionDateAsc(scopeType, scopeId).stream()
                .map(this::toDomainException)
                .toList();
    }

    private OperatingHourException addException(UUID tenantId, String scopeType, UUID scopeId, LocalDate date,
                                                boolean closed, LocalTime openTime, LocalTime closeTime, String reason) {
        OperatingHourException domain = new OperatingHourException(
                UUID.randomUUID(), tenantId, scopeType, scopeId, date, closed, openTime, closeTime, reason);
        OperatingHourExceptionJpaEntity entity = new OperatingHourExceptionJpaEntity(
                domain.id(), domain.tenantId(), domain.scopeType(), domain.scopeId(),
                domain.date(), domain.closed(), domain.openTime(), domain.closeTime(), domain.reason(), Instant.now());
        exceptionRepo.save(entity);
        return domain;
    }

    private OperatingHourException toDomainException(OperatingHourExceptionJpaEntity entity) {
        return new OperatingHourException(
                entity.getId(), entity.getTenantId(), entity.getScopeType(), entity.getScopeId(),
                entity.getExceptionDate(), entity.isClosed(), entity.getOpenTime(), entity.getCloseTime(),
                entity.getReason());
    }

    private UUID resolveTenantId(UUID workshopId) {
        UUID ctx = TenantContextHolder.getTenantId();
        return ctx != null ? ctx : workshopId;
    }
}
