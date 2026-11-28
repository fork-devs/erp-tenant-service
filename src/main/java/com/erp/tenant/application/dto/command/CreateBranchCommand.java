package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record CreateBranchCommand(
        UUID workshopId,
        String code,
        String name,
        String address,
        String phone,
        Double latitude,
        Double longitude,
        String coverImageUrl
) {
    public CreateBranchCommand(UUID workshopId, String code, String name, String address, String phone) {
        this(workshopId, code, name, address, phone, null, null, null);
    }

    public CreateBranchCommand(UUID workshopId, String code, String name, String address, String phone, Double latitude, Double longitude) {
        this(workshopId, code, name, address, phone, latitude, longitude, null);
    }
}
