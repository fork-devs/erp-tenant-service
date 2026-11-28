package com.erp.tenant.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Value object representing a weekly operating hour slot for a single day.
 */
public record OperatingHourEntry(
        DayOfWeek dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime,
        boolean active
) {
    public OperatingHourEntry {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("dayOfWeek is required");
        }
        if (active) {
            if (openTime == null || closeTime == null) {
                throw new IllegalArgumentException("openTime and closeTime are required when active");
            }
            if (!openTime.isBefore(closeTime)) {
                throw new IllegalArgumentException("openTime must be strictly before closeTime");
            }
        }
    }
}
