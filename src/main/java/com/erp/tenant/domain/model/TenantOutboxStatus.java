package com.erp.tenant.domain.model;

/**
 * Estado de un mensaje de la bandeja de salida.
 *
 * <p>El estado es explicito y no un booleano: con un booleano no se puede
 * distinguir un mensaje recien creado de uno que lleva horas fallando, ni
 * reconocer uno que ya no va a prosperar.
 */
public enum TenantOutboxStatus {

    /** Pendiente de publicar, con su proximo reintento programado. */
    PENDING,

    /** Publicado en el bus. */
    PUBLISHED,

    /** Agotado sus intentos: ya no se reintenta solo, pero se conserva. */
    DEAD_LETTER
}
