package com.erp.tenant.interfaces.rest.controller;

import com.erp.tenant.application.usecase.ListDeadLetteredMessagesUseCase;
import com.erp.tenant.application.usecase.ReactivateDeadLetteredMessageUseCase;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.model.TenantOutboxStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Administracion de la cola de mensajes agotados de la bandeja de salida.
 *
 * <p>Un mensaje agotado ya no se reintenta solo, pero se conserva: es la
 * evidencia de un evento que el sistema afirma haber producido y que nunca
 * llego al bus. Se puede reactivar para que vuelva a intentarse.
 */
@RestController
@RequestMapping("/api/v1/admin/outbox")
@Tag(name = "Outbox Administration", description = "Consulta y reactivacion de los mensajes de la bandeja de salida que agotaron sus intentos")
public class OutboxAdminController {

    private static final int MAX_LIMIT = 200;

    private final ListDeadLetteredMessagesUseCase listDeadLetteredMessagesUseCase;
    private final ReactivateDeadLetteredMessageUseCase reactivateDeadLetteredMessageUseCase;

    public OutboxAdminController(ListDeadLetteredMessagesUseCase listDeadLetteredMessagesUseCase,
                                 ReactivateDeadLetteredMessageUseCase reactivateDeadLetteredMessageUseCase) {
        this.listDeadLetteredMessagesUseCase = listDeadLetteredMessagesUseCase;
        this.reactivateDeadLetteredMessageUseCase = reactivateDeadLetteredMessageUseCase;
    }

    @GetMapping("/dead-letters")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Consultar la cola de mensajes agotados",
            description = "Devuelve los mensajes de la bandeja de salida que agotaron sus intentos, con su ultimo error y su numero de intentos")
    public ResponseEntity<List<OutboxDeadLetterResponse>> listDeadLetters(
            @RequestParam(value = "tenantId", required = false) UUID tenantId,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        int effectiveLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        List<OutboxDeadLetterResponse> responses = listDeadLetteredMessagesUseCase.list(tenantId, effectiveLimit).stream()
                .map(OutboxDeadLetterResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/dead-letters/{id}/reactivate")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN_TALLER')")
    @Operation(summary = "Reactivar un mensaje agotado",
            description = "Devuelve el mensaje a la cola de pendientes con el contador de intentos restablecido")
    public ResponseEntity<OutboxDeadLetterResponse> reactivate(@PathVariable UUID id) {
        TenantOutboxMessage message = reactivateDeadLetteredMessageUseCase.reactivate(id);
        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(OutboxDeadLetterResponse.from(message));
    }

    public record OutboxDeadLetterResponse(
            UUID id,
            UUID eventId,
            UUID aggregateId,
            UUID tenantId,
            String eventType,
            TenantOutboxStatus status,
            int attempts,
            String lastError,
            Instant nextAttemptAt,
            Instant createdAt) {

        static OutboxDeadLetterResponse from(TenantOutboxMessage message) {
            return new OutboxDeadLetterResponse(
                    message.getId(),
                    message.getEventId(),
                    message.getAggregateId(),
                    message.getTenantId(),
                    message.getEventType(),
                    message.getStatus(),
                    message.getAttempts(),
                    message.getLastError(),
                    message.getNextAttemptAt(),
                    message.getCreatedAt());
        }
    }
}
