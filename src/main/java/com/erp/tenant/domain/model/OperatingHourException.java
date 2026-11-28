package com.erp.tenant.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Value object representing a holiday or special-hours exception for a workshop or branch.
 */
public record OperatingHourException(
        UUID id,
        UUID tenantId,
        String scopeType,
        UUID scopeId,
        LocalDate date,
        boolean closed,
        LocalTime openTime,
        LocalTime closeTime,
        String reason
) {
    public OperatingHourException {
        if (date == null) {
            throw new IllegalArgumentException("exception date is required");
        }
        if (!closed) {
            if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
                throw new IllegalArgumentException("openTime must be strictly before closeTime for non-closed exceptions");
            }
        }
    }
}
