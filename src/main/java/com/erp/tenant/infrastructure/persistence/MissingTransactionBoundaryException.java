package com.erp.tenant.infrastructure.persistence;

/**
 * Infrastructure exception thrown when repository writes occur outside an active transaction boundary.
 */
public class MissingTransactionBoundaryException extends RuntimeException {

    public MissingTransactionBoundaryException(String operacion) {
        super("La operacion de persistencia '" + operacion
                + "' requiere un limite transaccional abierto por la capa de aplicacion. "
                + "Los adaptadores se unen a la transaccion en curso; no abren una propia.");
    }
}
