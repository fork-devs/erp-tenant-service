package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateBrandingRequest(
        String commercialName,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color de marca debe ser un código hexadecimal válido (ej. #1E40AF)")
        String brandColor
) {}
