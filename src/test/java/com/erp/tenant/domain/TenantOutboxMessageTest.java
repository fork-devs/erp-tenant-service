package com.erp.tenant.domain;

import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.model.TenantOutboxStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El estado de un mensaje de la bandeja de salida: los intentos, la espera
 * creciente y el paso a cola de muertos.
 */
class TenantOutboxMessageTest {

    private static final Duration MAX_DELAY = Duration.ofHours(24);
    private static final int MAX_ATTEMPTS = 7;

    private TenantOutboxMessage mensaje() {
        return TenantOutboxMessage.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TenantProvisioned",
                "{}");
    }

    @Test
    void unMensajeNuevoEstaPendienteSinIntentos() {
        TenantOutboxMessage message = mensaje();

        assertEquals(TenantOutboxStatus.PENDING, message.getStatus());
        assertEquals(0, message.getAttempts());
        assertNull(message.getLastError());
        assertNotNull(message.getEventId());
    }

    @Test
    void laEsperaEsCrecienteEntreIntentos() {
        TenantOutboxMessage message = mensaje();

        Duration anterior = Duration.ZERO;
        for (int intento = 1; intento < MAX_ATTEMPTS; intento++) {
            message.recordFailure("fallo " + intento, MAX_ATTEMPTS, MAX_DELAY);
            Duration espera = Duration.between(Instant.now(), message.getNextAttemptAt());
            assertTrue(espera.compareTo(anterior) > 0,
                    "La espera debe crecer: " + espera + " no es mayor que " + anterior);
            assertTrue(espera.compareTo(MAX_DELAY) <= 0, "La espera no puede superar el maximo configurado");
            anterior = espera;
        }
    }

    @Test
    void laEsperaNuncaSuperaElMaximoConfigurado() {
        TenantOutboxMessage message = mensaje();
        Duration maximo = Duration.ofSeconds(1);

        message.recordFailure("fallo", MAX_ATTEMPTS, maximo);

        Duration espera = Duration.between(Instant.now(), message.getNextAttemptAt());
        assertTrue(espera.compareTo(maximo) <= 0, "La espera debe respetar el maximo: " + espera);
    }

    @Test
    void alAgotarLosIntentosElMensajePasaAColaDeMuertos() {
        TenantOutboxMessage message = mensaje();

        for (int intento = 1; intento < MAX_ATTEMPTS; intento++) {
            message.recordFailure("fallo " + intento, MAX_ATTEMPTS, MAX_DELAY);
            assertEquals(TenantOutboxStatus.PENDING, message.getStatus());
            assertFalse(message.isDeadLettered());
        }

        message.recordFailure("fallo " + MAX_ATTEMPTS, MAX_ATTEMPTS, MAX_DELAY);

        assertEquals(TenantOutboxStatus.DEAD_LETTER, message.getStatus());
        assertTrue(message.isDeadLettered());
        assertEquals(MAX_ATTEMPTS, message.getAttempts());
        assertEquals("fallo " + MAX_ATTEMPTS, message.getLastError());
        assertNull(message.getNextAttemptAt(), "Un mensaje agotado no se reprograma");
    }

    @Test
    void reactivarDevuelveElMensajeAPendienteConElContadorRestablecido() {
        TenantOutboxMessage message = mensaje();
        for (int intento = 1; intento <= MAX_ATTEMPTS; intento++) {
            message.recordFailure("fallo", MAX_ATTEMPTS, MAX_DELAY);
        }
        assertTrue(message.isDeadLettered());

        message.reactivate();

        assertEquals(TenantOutboxStatus.PENDING, message.getStatus());
        assertEquals(0, message.getAttempts());
        assertNull(message.getLastError());
        assertNotNull(message.getNextAttemptAt());
    }

    @Test
    void marcarComoPublicadoLimpiaElError() {
        TenantOutboxMessage message = mensaje();
        message.recordFailure("fallo", MAX_ATTEMPTS, MAX_DELAY);

        message.markAsPublished();

        assertEquals(TenantOutboxStatus.PUBLISHED, message.getStatus());
        assertTrue(message.isPublished());
        assertNull(message.getLastError());
    }
}
