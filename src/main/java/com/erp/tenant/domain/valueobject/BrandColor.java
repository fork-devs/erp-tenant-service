package com.erp.tenant.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public final class BrandColor implements Serializable {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    private final String value;

    public BrandColor(String value) {
        if (value == null || value.trim().isEmpty()) {
            this.value = "#2563EB"; // Color azul por defecto
            return;
        }
        String normalized = value.trim();
        if (!HEX_COLOR_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Color hexadecimal inválido (ej. #1E40AF): " + value);
        }
        this.value = normalized.toUpperCase();
    }

    public static BrandColor of(String value) {
        return new BrandColor(value);
    }

    public static BrandColor defaultColor() {
        return new BrandColor("#2563EB");
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrandColor that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
