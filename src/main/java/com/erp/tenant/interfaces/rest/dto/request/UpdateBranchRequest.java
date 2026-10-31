package com.erp.tenant.interfaces.rest.dto.request;

public record UpdateBranchRequest(
        String code,
        String name,
        String address,
        String phone
) {}
