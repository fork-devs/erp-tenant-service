package com.erp.tenant.domain.exception;

public class RucNotActiveException extends DomainException {
    public RucNotActiveException(String ruc, String status, String condition) {
        super("RUC_NOT_FOUND_OR_INACTIVE", "El RUC " + ruc + " no es valido ante SUNAT o no se encuentra en estado ACTIVO y condición HABIDO (Estado: " + status + ", Condición: " + condition + ")");
    }

    public RucNotActiveException(String ruc) {
        super("RUC_NOT_FOUND_OR_INACTIVE", "El RUC " + ruc + " no existe o no figura registrado en el padrón de SUNAT");
    }
}
