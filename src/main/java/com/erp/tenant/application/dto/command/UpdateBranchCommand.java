package com.erp.tenant.application.dto.command;

import java.util.UUID;

public record UpdateBranchCommand(
        UUID branchId,
        String code,
        String name,
        String address,
        String phone
) {}
