package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record CreateBranchCommand(
        UUID workshopId,
        String code,
        String name,
        String address,
        String phone
) {}
