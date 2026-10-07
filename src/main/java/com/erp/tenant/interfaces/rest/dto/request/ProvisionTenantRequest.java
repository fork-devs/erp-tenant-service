package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ProvisionTenantRequest(
        @NotBlank(message = "El nombre comercial del taller es obligatorio")
        String commercialName,

        @NotBlank(message = "El RUC del taller es obligatorio")
        @Pattern(regexp = "^(10|20|15|17)\\d{9}$", message = "El RUC debe tener 11 dígitos y comenzar con 10, 20, 15 o 17")
        String ruc,

        String planCode,

        UUID planId,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color de marca debe ser un código hexadecimal válido (ej. #1E40AF)")
        String brandColor,

        String adminEmail,

        String adminFullName
) {}
