package com.erp.tenant.domain.exception;

public class ProfileNotFoundException extends DomainException {
    public ProfileNotFoundException(String message) {
        super("PROFILE_NOT_FOUND", message);
    }
}
