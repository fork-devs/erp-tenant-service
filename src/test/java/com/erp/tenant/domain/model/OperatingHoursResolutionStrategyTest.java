package com.erp.tenant.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OperatingHoursResolutionStrategyTest {

    private final OperatingHoursResolutionStrategy strategy = new OperatingHoursResolutionStrategy();

    @Test
    void branchExceptionOverridesWorkshopAndWeeklySchedules() {
        UUID branchId = UUID.randomUUID();
        LocalDate holiday = LocalDate.of(2026, 7, 28); // Tuesday
        OperatingHourException branchEx = new OperatingHourException(
                UUID.randomUUID(), branchId, "BRANCH", branchId, holiday, true, null, null, "Independence Day");
        List<OperatingHourEntry> workshopWeekly = List.of(
                new OperatingHourEntry(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0), true));

        OperatingHoursResolutionStrategy.EffectiveAvailability result = strategy.resolve(
                branchId, holiday, Optional.of(branchEx), Optional.empty(), List.of(), workshopWeekly);

        assertFalse(result.open());
        assertEquals("BRANCH_EXCEPTION", result.source());
        assertEquals("Independence Day", result.reason());
    }

    @Test
    void branchScheduleOverridesWorkshopDefault() {
        UUID branchId = UUID.randomUUID();
        LocalDate monday = LocalDate.of(2026, 11, 30);
        List<OperatingHourEntry> branchWeekly = List.of(
                new OperatingHourEntry(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), true));
        List<OperatingHourEntry> workshopWeekly = List.of(
                new OperatingHourEntry(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), true));

        OperatingHoursResolutionStrategy.EffectiveAvailability result = strategy.resolve(
                branchId, monday, Optional.empty(), Optional.empty(), branchWeekly, workshopWeekly);

        assertTrue(result.open());
        assertEquals("BRANCH_SCHEDULE", result.source());
        assertEquals(LocalTime.of(9, 0), result.openTime());
        assertEquals(LocalTime.of(17, 0), result.closeTime());
    }

    @Test
    void fallsBackToWorkshopScheduleWhenBranchHasNoOverride() {
        UUID branchId = UUID.randomUUID();
        LocalDate monday = LocalDate.of(2026, 11, 30);
        List<OperatingHourEntry> workshopWeekly = List.of(
                new OperatingHourEntry(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(19, 0), true));

        OperatingHoursResolutionStrategy.EffectiveAvailability result = strategy.resolve(
                branchId, monday, Optional.empty(), Optional.empty(), List.of(), workshopWeekly);

        assertTrue(result.open());
        assertEquals("WORKSHOP_SCHEDULE", result.source());
        assertEquals(LocalTime.of(8, 0), result.openTime());
    }

    @Test
    void rejectsInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new OperatingHourEntry(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(8, 0), true));
    }
}
