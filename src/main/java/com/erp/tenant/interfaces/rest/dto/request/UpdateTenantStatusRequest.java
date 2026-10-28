package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantStatusRequest(
        @NotBlank(message = "{validation.tenant.status.action.not-blank}")
        String action,

        String reason
) {}
