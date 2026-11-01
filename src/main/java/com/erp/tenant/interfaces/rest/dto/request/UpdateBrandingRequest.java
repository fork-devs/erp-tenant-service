package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateBrandingRequest(
        String commercialName,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "{validation.tenant.branding.brand-color.pattern}")
        String brandColor
) {}
