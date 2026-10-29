package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ProvisionTenantRequest(
        @NotBlank(message = "{validation.tenant.provision.commercial-name.not-blank}")
        String commercialName,

        @NotBlank(message = "{validation.tenant.provision.ruc.not-blank}")
        @Pattern(regexp = "^(10|20|15|17)\\d{9}$", message = "{validation.tenant.provision.ruc.pattern}")
        String ruc,

        String planCode,

        UUID planId,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "{validation.tenant.provision.brand-color.pattern}")
        String brandColor,

        String adminEmail,

        String adminFullName
) {}
