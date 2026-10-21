package com.erp.tenant.application.service;

import com.erp.tenant.application.usecase.ProcessOutboxMessagesUseCase;
import com.erp.tenant.domain.model.TenantOutboxMessage;
import com.erp.tenant.domain.port.EventPublisherPort;
import com.erp.tenant.domain.port.TenantOutboxRepositoryPort;
import com.erp.tenant.domain.port.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Despacha los mensajes pendientes de la bandeja de salida.
 *
 * <p>Tres propiedades que el relay anterior no tenia:
 *
 * <ul>
 *   <li>Un mensaje que falla <strong>no bloquea</strong> el resto de la cola.
 *       Antes, un solo mensaje envenenado detenia el lote entero de forma
 *       permanente, porque la cola se leia ordenada por fecha de creacion y el
 *       fallido siempre era el primero.</li>
 *   <li>Cada fallo <strong>queda registrado</strong> con su error, su numero
 *       de intento y la espera hasta el proximo. Antes la excepcion se
 *       tragaba sin dejar rastro.</li>
 *   <li>La publicacion ocurre <strong>fuera</strong> de la transaccion de
 *       negocio que confirmo el cambio, y con la transaccion de la bandeja
 *       de salida ya cerrada. Un fallo de publicacion no puede deshacer un
 *       cambio de negocio ya confirmado.</li>
 * </ul>
 */
public class OutboxProcessorService implements ProcessOutboxMessagesUseCase {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessorService.class);

    private final TenantOutboxRepositoryPort outboxRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final UnitOfWork unitOfWork;
    private final int maxAttempts;
    private final Duration maxDelay;

    public OutboxProcessorService(TenantOutboxRepositoryPort outboxRepositoryPort,
                                  EventPublisherPort eventPublisherPort,
                                  UnitOfWork unitOfWork,
                                  int maxAttempts,
                                  Duration maxDelay) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.unitOfWork = unitOfWork;
        this.maxAttempts = maxAttempts;
        this.maxDelay = maxDelay;
    }

    @Override
    public int processUnpublishedMessages(int batchSize) {
        // La lectura con reclamacion de la fila se hace en su propia transaccion, que
        // se cierra al salir de este metodo. La publicacion, por tanto, ya no
        // forma parte de ninguna transaccion.
        List<TenantOutboxMessage> messages = outboxRepositoryPort.findPending(Instant.now(), batchSize);

        int published = 0;
        for (TenantOutboxMessage message : messages) {
            try {
                eventPublisherPort.publish(message);
                message.markAsPublished();
                unitOfWork.runInTransaction(() -> outboxRepositoryPort.save(message));
                published++;
            } catch (Exception e) {
                // Un mensaje fallido no detiene el lote: se registra y se
                // reprograma, y el siguiente mensaje se despacha igualmente.
                log.warn("[OUTBOX-RELAY] No se pudo publicar el evento {} ({}). Intento {}/{}: {}",
                        message.getEventType(), message.getId(), message.getAttempts() + 1, maxAttempts, e.getMessage());
                message.recordFailure(e.getMessage(), maxAttempts, maxDelay);
                unitOfWork.runInTransaction(() -> outboxRepositoryPort.save(message));
            }
        }
        return published;
    }
}
