package com.erp.tenant.domain.exception;

public class DocumentNotFoundException extends DomainException {
    public DocumentNotFoundException(String documentNumber) {
        super("DOCUMENT_NOT_FOUND_RENIEC", "El documento de identidad " + documentNumber + " no existe o no pudo ser verificado ante RENIEC");
    }
}
