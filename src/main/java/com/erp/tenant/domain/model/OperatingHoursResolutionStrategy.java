package com.erp.tenant.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Strategy for resolving effective branch availability on a target date.
 * Precedence: Branch exception > Workshop exception > Branch weekly schedule > Workshop weekly schedule.
 */
public final class OperatingHoursResolutionStrategy {

    public record EffectiveAvailability(
            UUID branchId,
            LocalDate date,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            String source,
            String reason
    ) {}

    public EffectiveAvailability resolve(
            UUID branchId,
            LocalDate date,
            Optional<OperatingHourException> branchException,
            Optional<OperatingHourException> workshopException,
            List<OperatingHourEntry> branchWeekly,
            List<OperatingHourEntry> workshopWeekly
    ) {
        if (branchException.isPresent()) {
            OperatingHourException ex = branchException.get();
            return new EffectiveAvailability(
                    branchId, date, !ex.closed(), ex.openTime(), ex.closeTime(),
                    "BRANCH_EXCEPTION", ex.reason()
            );
        }
        if (workshopException.isPresent()) {
            OperatingHourException ex = workshopException.get();
            return new EffectiveAvailability(
                    branchId, date, !ex.closed(), ex.openTime(), ex.closeTime(),
                    "WORKSHOP_EXCEPTION", ex.reason()
            );
        }

        DayOfWeek day = date.getDayOfWeek();
        List<OperatingHourEntry> effectiveWeekly = branchWeekly.isEmpty() ? workshopWeekly : branchWeekly;
        String source = branchWeekly.isEmpty() ? "WORKSHOP_SCHEDULE" : "BRANCH_SCHEDULE";

        return effectiveWeekly.stream()
                .filter(e -> e.dayOfWeek() == day && e.active())
                .findFirst()
                .map(e -> new EffectiveAvailability(branchId, date, true, e.openTime(), e.closeTime(), source, null))
                .orElseGet(() -> new EffectiveAvailability(branchId, date, false, null, null, source, "Closed on " + day));
    }
}
