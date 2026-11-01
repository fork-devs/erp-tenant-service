package com.erp.tenant.domain.exception;

public class DocumentAlreadyExistsException extends DomainException {
    public DocumentAlreadyExistsException(String documentNumber) {
        super("DOCUMENT_ALREADY_EXISTS", "Document number is already registered: " + documentNumber);
    }
}
