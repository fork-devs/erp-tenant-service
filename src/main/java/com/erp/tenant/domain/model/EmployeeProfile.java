package com.erp.tenant.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an employee profile.
 */
public class EmployeeProfile implements Serializable {

    private final UUID id;
    private final UUID tenantId;
    private final UUID userId;
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String documentNumber;
    private String phone;
    private final Instant createdAt;
    private Instant updatedAt;

    public EmployeeProfile(UUID id,
                           UUID tenantId,
                           UUID userId,
                           String firstName,
                           String lastName,
                           DocumentType documentType,
                           String documentNumber,
                           String phone,
                           Instant createdAt,
                           Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Employee profile ID is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (documentType == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required");
        }
        validateDocumentNumber(documentType, documentNumber.trim());

        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.documentType = documentType;
        this.documentNumber = documentNumber.trim();
        this.phone = phone != null ? phone.trim() : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static EmployeeProfile create(UUID tenantId,
                                         UUID userId,
                                         String firstName,
                                         String lastName,
                                         DocumentType documentType,
                                         String documentNumber,
                                         String phone) {
        Instant now = Instant.now();
        return new EmployeeProfile(
                UUID.randomUUID(),
                tenantId,
                userId,
                firstName,
                lastName,
                documentType,
                documentNumber,
                phone,
                now,
                now
        );
    }

    public static void validateDocumentNumber(DocumentType type, String number) {
        if (type == DocumentType.DNI) {
            if (!number.matches("^[0-9]{8}$")) {
                throw new IllegalArgumentException("DNI must be exactly 8 digits");
            }
        } else if (type == DocumentType.RUC) {
            if (!number.matches("^[0-9]{11}$")) {
                throw new IllegalArgumentException("RUC must be exactly 11 digits");
            }
        }
    }

    public void update(String firstName,
                       String lastName,
                       DocumentType documentType,
                       String documentNumber,
                       String phone) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName.trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName.trim();
        }
        if (documentType != null) {
            this.documentType = documentType;
        }
        if (documentNumber != null && !documentNumber.trim().isEmpty()) {
            validateDocumentNumber(this.documentType, documentNumber.trim());
            this.documentNumber = documentNumber.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone.trim();
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeProfile that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
