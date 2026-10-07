package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantStatusRequest(
        @NotBlank(message = "La acción de estado es obligatoria (SUSPEND o REACTIVATE)")
        String action,

        String reason
) {}
