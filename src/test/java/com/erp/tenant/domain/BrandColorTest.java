package com.erp.tenant.domain;

import com.erp.tenant.domain.valueobject.BrandColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BrandColorTest {

    @Test
    @DisplayName("Should use default color when null or blank is passed")
    void shouldUseDefaultColorWhenNullOrBlank() {
        assertEquals("#2563EB", BrandColor.of(null).getValue());
        assertEquals("#2563EB", BrandColor.of("").getValue());
        assertEquals("#2563EB", BrandColor.of("   ").getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#1E40AF", "#FF5733", "#000000", "#FFFFFF", "#abcdef", "#FFF"})
    @DisplayName("Should accept valid hex colors and normalize to uppercase")
    void shouldAcceptValidHexColor(String hex) {
        BrandColor color = BrandColor.of(hex);
        assertEquals(hex.toUpperCase(), color.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1E40AF", "#GGGGGG", "red", "#1234567", "#12"})
    @DisplayName("Should reject invalid hex color formats")
    void shouldRejectInvalidHex(String invalidHex) {
        assertThrows(IllegalArgumentException.class, () -> BrandColor.of(invalidHex));
    }
}
