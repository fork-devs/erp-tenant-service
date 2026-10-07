package com.erp.tenant.domain.valueobject;

import com.erp.tenant.domain.exception.InvalidRucException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Ruc implements Serializable {
    private static final Pattern RUC_PATTERN = Pattern.compile("^(10|20|15|17)[0-9]{9}$");

    private final String value;

    public Ruc(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRucException("El número de RUC no puede estar vacío");
        }
        String normalized = value.trim();
        if (!RUC_PATTERN.matcher(normalized).matches()) {
            throw new InvalidRucException("El RUC debe tener 11 dígitos y comenzar con 10, 20, 15 o 17: " + value);
        }
        this.value = normalized;
    }

    public static Ruc of(String value) {
        return new Ruc(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ruc ruc)) return false;
        return Objects.equals(value, ruc.value);
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
