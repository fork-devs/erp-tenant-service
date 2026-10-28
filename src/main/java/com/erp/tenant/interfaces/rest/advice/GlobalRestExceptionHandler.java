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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalRestExceptionHandler() {
        this(null);
    }

    public GlobalRestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String getMessage(String code, String defaultMessage, Object... args) {
        if (messageSource == null) {
            return defaultMessage;
        }
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    @ExceptionHandler(RucAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleRucAlreadyExists(RucAlreadyExistsException ex) {
        String detail = getMessage("error.tenant.ruc-already-exists.detail", ex.getMessage());
        String title = getMessage("error.tenant.ruc-already-exists.title", "RUC ya registrado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/ruc-conflict"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTenantNotFound(TenantNotFoundException ex) {
        String detail = getMessage("error.tenant.tenant-not-found.detail", ex.getMessage());
        String title = getMessage("error.tenant.tenant-not-found.title", "Taller Inquilino No Encontrado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/tenant-not-found"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePlanNotFound(PlanNotFoundException ex) {
        String detail = getMessage("error.tenant.plan-not-found.detail", ex.getMessage());
        String title = getMessage("error.tenant.plan-not-found.title", "Plan de Suscripción No Encontrado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/plan-not-found"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidRucException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRuc(InvalidRucException ex) {
        String detail = getMessage("error.tenant.invalid-ruc.detail", ex.getMessage());
        String title = getMessage("error.tenant.invalid-ruc.title", "RUC Inválido");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/invalid-ruc"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(TenantSuspendedException.class)
    public ResponseEntity<ProblemDetail> handleTenantSuspended(TenantSuspendedException ex) {
        String detail = getMessage("error.tenant.tenant-suspended.detail", ex.getMessage());
        String title = getMessage("error.tenant.tenant-suspended.title", "Taller Suspendido");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/tenant-suspended"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleGenericDomainException(DomainException ex) {
        String detail = getMessage("error.tenant.domain-rule-violation.detail", ex.getMessage());
        String title = getMessage("error.tenant.domain-rule-violation.title", "Regla de Dominio Violada");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/tenant/domain-rule-violation"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleTenantAccessDenied(TenantAccessDeniedException ex) {
        String detail = getMessage("error.tenant.access-denied.detail", ex.getMessage());
        String title = getMessage("error.tenant.access-denied.title", "Acceso Denegado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/access-denied"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(TenantContextMissingException.class)
    public ResponseEntity<ProblemDetail> handleTenantContextMissing(TenantContextMissingException ex) {
        String detail = getMessage("error.tenant.context-missing.detail", ex.getMessage());
        String title = getMessage("error.tenant.context-missing.title", "Contexto de Taller Ausente");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/context-missing"));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    /**
     * Handles method-level authorization denials intercepted prior to filter chain resolution.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        String detail = getMessage("error.tenant.access-denied.detail", "No tiene permisos para realizar esta operacion.");
        String title = getMessage("error.tenant.access-denied.title", "Acceso Denegado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotriz.com/tenant/access-denied"));
        problem.setProperty("errorCode", "ACCESS_DENIED");
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String detail = getMessage("error.tenant.validation-error.detail", "Error de validación en los campos del request");
        String title = getMessage("error.tenant.validation-error.title", "Validación Fallida");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/validation/invalid-fields"));

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("invalidParams", errors);
        problem.setProperty("errorCode", "VALIDATION_ERROR");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        String detail = getMessage("error.tenant.illegal-argument.detail", ex.getMessage());
        String title = getMessage("error.tenant.illegal-argument.title", "Parámetro Inválido");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/bad-request/illegal-argument"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        String detail = getMessage("error.tenant.illegal-state.detail", ex.getMessage());
        String title = getMessage("error.tenant.illegal-state.title", "Conflicto de Estado");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/conflict/illegal-state"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        // Internal server errors are logged with structured context and stack trace.
        com.erp.tenant.application.logging.SecurityEventLogger.registrar(
                "error_interno", null, null, null, "fallo",
                java.util.Map.of("error", ex.getClass().getSimpleName()));
        log.error("Unhandled internal server error occurred: ", ex);
        String detail = getMessage("error.tenant.internal-error.detail", "Ha ocurrido un error interno inesperado.");
        String title = getMessage("error.tenant.internal-error.title", "Error Interno del Servidor");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://errors.erp-automotive.pe/server-error"));
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
