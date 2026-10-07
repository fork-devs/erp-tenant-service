package com.erp.tenant.domain;

import com.erp.tenant.domain.exception.InvalidRucException;
import com.erp.tenant.domain.valueobject.Ruc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RucTest {

    @ParameterizedTest
    @ValueSource(strings = {"20601234567", "10456789012", "15987654321", "17123456789"})
    @DisplayName("Should accept valid Peruvian RUC numbers")
    void shouldAcceptValidRuc(String validRuc) {
        Ruc ruc = Ruc.of(validRuc);
        assertNotNull(ruc);
        assertEquals(validRuc, ruc.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "30601234567", // Invalid prefix (30)
            "12345678901", // Invalid prefix (12)
            "2060123456",  // 10 digits (too short)
            "206012345678", // 12 digits (too long)
            "2060123456A", // Non-digit characters
            "",            // Empty
            "           "  // Blank
    })
    @DisplayName("Should reject invalid RUC formats")
    void shouldRejectInvalidRuc(String invalidRuc) {
        assertThrows(InvalidRucException.class, () -> Ruc.of(invalidRuc));
    }

    @Test
    @DisplayName("Should reject null RUC")
    void shouldRejectNullRuc() {
        assertThrows(InvalidRucException.class, () -> Ruc.of(null));
    }

    @Test
    @DisplayName("Should test equality by value")
    void shouldTestEquality() {
        Ruc ruc1 = Ruc.of("20601234567");
        Ruc ruc2 = Ruc.of("20601234567");
        Ruc ruc3 = Ruc.of("20609999999");

        assertEquals(ruc1, ruc2);
        assertNotEquals(ruc1, ruc3);
        assertEquals(ruc1.hashCode(), ruc2.hashCode());
    }
}
