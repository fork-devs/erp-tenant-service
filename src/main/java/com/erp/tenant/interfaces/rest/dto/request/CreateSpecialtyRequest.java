package com.erp.tenant.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialtyRequest(
        @NotBlank(message = "El nombre de la especialidad es obligatorio")
        @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
        String name,

        @NotBlank(message = "El código de la especialidad es obligatorio")
        @Size(max = 100, message = "El código no puede exceder 100 caracteres")
        String code,

        String description
) {}
