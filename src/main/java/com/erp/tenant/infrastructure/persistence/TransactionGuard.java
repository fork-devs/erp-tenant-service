package com.erp.tenant.infrastructure.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Guard utility verifying that an active database transaction boundary exists before modifying data.
 */
@Component
public class TransactionGuard {

    public void requireActiveTransaction(String operacion) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new MissingTransactionBoundaryException(operacion);
        }
    }
}
