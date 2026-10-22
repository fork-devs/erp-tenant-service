package com.erp.tenant.interfaces.rest.advice;

import com.erp.tenant.domain.exception.DomainException;
import com.erp.tenant.domain.exception.TenantAccessDeniedException;
import com.erp.tenant.domain.exception.TenantContextMissingException;
import com.erp.tenant.domain.exception.InvalidRucException;
import com.erp.tenant.domain.exception.PlanNotFoundException;
import com.erp.tenant.domain.exception.RucAlreadyExistsException;
import com.erp.tenant.domain.exception.TenantNotFoundException;
import com.erp.tenant.domain.exception.TenantSuspendedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(RucAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleRucAlreadyExists(RucAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("RUC ya registrado");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/ruc-conflict"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTenantNotFound(TenantNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Taller Inquilino No Encontrado");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/tenant-not-found"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePlanNotFound(PlanNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Plan de Suscripción No Encontrado");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/plan-not-found"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidRucException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRuc(InvalidRucException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("RUC Inválido");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/invalid-ruc"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(TenantSuspendedException.class)
    public ResponseEntity<ProblemDetail> handleTenantSuspended(TenantSuspendedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Taller Suspendido");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/tenant-suspended"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleGenericDomainException(DomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Regla de Dominio Violada");
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/domain-rule-violation"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleTenantAccessDenied(TenantAccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Acceso Denegado");
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/access-denied"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(TenantContextMissingException.class)
    public ResponseEntity<ProblemDetail> handleTenantContextMissing(TenantContextMissingException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Contexto de Taller Ausente");
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/context-missing"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * La denegacion de la seguridad a nivel de metodo se resuelve aqui y no en
     * el filtro de seguridad: el resolutor de excepciones del dispatcher la
     * intercepta antes de que llegue al filtro.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operacion.");
        problem.setTitle("Acceso Denegado");
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/access-denied"));
        problem.setProperty("errorCode", "ACCESS_DENIED");
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Error de validación en los campos del request");
        problem.setTitle("Validación Fallida");
        problem.setType(URI.create("https://errors.erp-automotive.pe/validation/invalid-fields"));

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("invalidParams", errors);
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Parámetro Inválido");
        problem.setType(URI.create("https://errors.erp-automotive.pe/bad-request/illegal-argument"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflicto de Estado");
        problem.setType(URI.create("https://errors.erp-automotive.pe/conflict/illegal-state"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        log.error("Unhandled internal server error occurred: ", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno inesperado.");
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("https://errors.erp-automotive.pe/server-error"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
