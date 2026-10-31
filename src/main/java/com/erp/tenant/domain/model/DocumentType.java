package com.erp.tenant.domain.model;

/**
 * Supported legal identification document types.
 */
public enum DocumentType {
    DNI,
    RUC,
    CE,
    PASSPORT;

    public static DocumentType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be empty");
        }
        try {
            return DocumentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid document type: " + value + ". Allowed: DNI, RUC, CE, PASSPORT");
        }
    }
}
