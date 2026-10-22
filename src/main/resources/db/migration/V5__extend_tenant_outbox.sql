-- =============================================================================
-- ERP TENANT SERVICE - V5: ESTADO E INTENTOS DE LA BANDEJA DE SALIDA
-- =============================================================================
-- La bandeja de salida solo sabia si un mensaje estaba publicado o no. Con esa
-- informacion el relay no puede distinguir un mensaje recien creado de uno que
-- lleva horas fallando, ni reintentar con una espera creciente, ni distinguir
-- un mensaje agotado de uno pendiente.
--
-- Se anaden el identificador del evento (para que el consumidor pueda
-- deduplicar), el contador de intentos, la fecha del proximo reintento, el
-- ultimo error y el estado explicito.
--
-- La columna `published` se conserva durante un ciclo de despliegue: la
-- eliminan las migraciones posteriores, cuando ningun proceso lee ya de ella.
--
-- Ver docs/doc-estado-implementacion.md, hallazgos P1 6.8 y P1 7.21.
-- =============================================================================

ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS event_id UUID;
ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS next_attempt_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS last_error TEXT;
ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- El identificador del evento ya viaja dentro del payload, de modo que se
-- recupera de ahi en lugar de inventar uno: un consumidor que ya habia
-- deduplicado con ese identificador sigue funcionando.
UPDATE tenant_outbox
   SET event_id = COALESCE(
           (SELECT (regexp_match(payload,
                   '"eventId":"([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"'))[1])::uuid,
           gen_random_uuid())
 WHERE event_id IS NULL;

-- El estado deriva del booleano historico: un mensaje ya publicado lo esta.
UPDATE tenant_outbox
   SET status = CASE WHEN published THEN 'PUBLISHED' ELSE 'PENDING' END;

-- La primera programacion de reintento es la propia fecha de creacion, de
-- modo que un mensaje pendiente pendiente de publicar sale de inmediato.
UPDATE tenant_outbox
   SET next_attempt_at = created_at
 WHERE next_attempt_at IS NULL;

ALTER TABLE tenant_outbox ALTER COLUMN event_id SET NOT NULL;

DO $$
DECLARE
    invalidos INTEGER;
BEGIN
    SELECT count(*) INTO invalidos
      FROM tenant_outbox
     WHERE status NOT IN ('PENDING', 'PUBLISHED', 'DEAD_LETTER');

    IF invalidos > 0 THEN
        RAISE EXCEPTION 'Hay % mensajes con un estado no valido', invalidos;
    END IF;

    SELECT count(*) INTO invalidos
      FROM tenant_outbox
     WHERE status = 'PENDING' AND next_attempt_at IS NULL;

    IF invalidos > 0 THEN
        RAISE EXCEPTION 'Hay % mensajes pendientes sin fecha de proximo intento', invalidos;
    END IF;
END
$$;

-- El relay lista por estado y fecha de reintento: el indice acompaña esa
-- consulta. El identificador del evento es unico, para que un consumidor
-- pueda deduplicar y para que no existan dos mensajes del mismo evento.
CREATE INDEX IF NOT EXISTS idx_tenant_outbox_pending
    ON tenant_outbox(next_attempt_at)
    WHERE status = 'PENDING';

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_outbox_event_id
    ON tenant_outbox(event_id);
