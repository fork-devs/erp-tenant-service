-- =============================================================================
-- ERP TENANT SERVICE - V3: TENANT_ID EN LA BANDEJA DE SALIDA
-- =============================================================================
-- La bandeja de salida de eventos de dominio no tenia columna tenant_id,
-- de modo que no podia quedar cubierta por una politica de Row-Level Security
-- ni filtrarse por taller. Es la tabla donde vive el dato mas sensible del
-- servicio: el RUC y la razon social del taller en el payload del evento.
--
-- Ver docs/doc-estado-implementacion.md, hallazgo P1 6.14.
-- =============================================================================

ALTER TABLE tenant_outbox ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Backfill: en este servicio el taller ES la fila, de modo que el tenant de
-- proprio taller. No hay filas huerfanas: aggregate_type es TENANT y
-- filas huerfanas: aggregate_type es TENANT y aggregate_id referencia a
-- tenants(id).
UPDATE tenant_outbox o
   SET tenant_id = t.id
  FROM tenants t
 WHERE o.aggregate_type = 'TENANT'
   AND t.id = o.aggregate_id
   AND o.tenant_id IS NULL;

-- Si quedara alguna fila sin tenant, no se puede inventar su valor: se falla
-- de forma explicita en lugar de dejar filas que la politica no podria
-- filtrar.
DO $$
DECLARE
    huerfanas INTEGER;
BEGIN
    SELECT count(*) INTO huerfanas
      FROM tenant_outbox
     WHERE tenant_id IS NULL;

    IF huerfanas > 0 THEN
        RAISE EXCEPTION
            'Hay % mensajes de la bandeja de salida sin taller resoluble: revisar la integridad de tenants', huerfanas;
    END IF;
END
$$;

ALTER TABLE tenant_outbox ALTER COLUMN tenant_id SET NOT NULL;

-- El relay lista por estado de publicacion; el indice combina el filtro con
-- el taller para que ambas consultas sean acotadas.
CREATE INDEX IF NOT EXISTS idx_tenant_outbox_tenant_pending
    ON tenant_outbox(tenant_id, created_at)
    WHERE published = FALSE;
