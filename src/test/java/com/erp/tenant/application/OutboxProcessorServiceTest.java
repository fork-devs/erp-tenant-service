package com.erp.tenant.application;

import com.erp.tenant.application.service.OutboxProcessorService;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.model.TenantOutboxStatus;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El relay no se bloquea con un mensaje envenenado y registra cada fallo.
 */
@ExtendWith(MockitoExtension.class)
class OutboxProcessorServiceTest {

    private static final int MAX_ATTEMPTS = 7;
    private static final Duration MAX_DELAY = Duration.ofHours(24);

    @Mock
    private TenantOutboxRepositoryPort outboxRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private OutboxProcessorService relay;

    @BeforeEach
    void setUp() {
        UnitOfWork directo = new UnitOfWork() {
            @Override
            public void runInTransaction(Runnable work) {
                work.run();
            }

            @Override
            public <T> T callInTransaction(java.util.function.Supplier<T> work) {
                return work.get();
            }
        };
        relay = new OutboxProcessorService(outboxRepositoryPort, eventPublisherPort, directo, MAX_ATTEMPTS, MAX_DELAY);
    }

    @Test
    @DisplayName("Un mensaje envenenado no impide publicar el siguiente del mismo ciclo")
    void unMensajeEnvenenadoNoBloqueaElResto() {
        TenantOutboxMessage envenenado = mensaje();
        TenantOutboxMessage valido = mensaje();

        when(outboxRepositoryPort.findPending(any(Instant.class), any(Integer.class)))
                .thenReturn(List.of(envenenado, valido));
        doThrow(new RuntimeException("el bus esta caido"))
                .when(eventPublisherPort).publish(envenenado);

        int publicados = relay.processUnpublishedMessages(50);

        assertEquals(1, publicados, "El mensaje valido debe publicarse en el mismo ciclo");
        verify(eventPublisherPort).publish(valido);
        assertEquals(TenantOutboxStatus.PUBLISHED, valido.getStatus());
        assertEquals(TenantOutboxStatus.PENDING, envenenado.getStatus(),
                "El mensaje fallido se reprograma, no se abandona");
        assertEquals(1, envenenado.getAttempts());
        assertEquals("el bus esta caido", envenenado.getLastError(),
                "El error del fallo debe quedar registrado");
    }

    @Test
    @DisplayName("Todos los mensajes fallidos se reprograman y ninguno se pierde")
    void todosLosFallosSePrograman() {
        TenantOutboxMessage primero = mensaje();
        TenantOutboxMessage segundo = mensaje();

        when(outboxRepositoryPort.findPending(any(Instant.class), any(Integer.class)))
                .thenReturn(List.of(primero, segundo));
        doThrow(new RuntimeException("caida")).when(eventPublisherPort).publish(any());

        relay.processUnpublishedMessages(50);

        assertEquals(1, primero.getAttempts());
        assertEquals(1, segundo.getAttempts());
        assertNotNullNextAttempt(primero);
        assertNotNullNextAttempt(segundo);
    }

    @Test
    @DisplayName("Un mensaje que se publica queda marcado como publicado")
    void unMensajePublicadoQuedaPublicado() {
        TenantOutboxMessage message = mensaje();
        when(outboxRepositoryPort.findPending(any(Instant.class), any(Integer.class)))
                .thenReturn(List.of(message));

        int publicados = relay.processUnpublishedMessages(50);

        assertEquals(1, publicados);
        assertEquals(TenantOutboxStatus.PUBLISHED, message.getStatus());
        verify(outboxRepositoryPort).save(message);
    }

    @Test
    @DisplayName("Un mensaje agotado no se vuelve a programar")
    void unMensajeAgotadoNoSeVuelveAProgramar() {
        TenantOutboxMessage message = mensaje();
        for (int intento = 1; intento <= MAX_ATTEMPTS; intento++) {
            message.recordFailure("fallo", MAX_ATTEMPTS, MAX_DELAY);
        }
        when(outboxRepositoryPort.findPending(any(Instant.class), any(Integer.class)))
                .thenReturn(List.of(message));
        doThrow(new RuntimeException("caida")).when(eventPublisherPort).publish(message);

        relay.processUnpublishedMessages(50);

        assertEquals(TenantOutboxStatus.DEAD_LETTER, message.getStatus());
    }

    private void assertNotNullNextAttempt(TenantOutboxMessage message) {
        org.junit.jupiter.api.Assertions.assertNotNull(message.getNextAttemptAt(),
                "Un mensaje fallido debe quedar programado para un reintento");
    }

    private TenantOutboxMessage mensaje() {
        return TenantOutboxMessage.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "TenantProvisioned", "{}");
    }
}
