-- ADR-007: los limites de trafico son parte del PLAN, y el plan lo posee el
-- tenant-service. Antes los deducía el gateway con un switch sobre el codigo
-- de plan, lo que era una segunda copia de la promesa comercial.
--
-- Los valores son los que el gateway aplicaba hasta ahora, de modo que el
-- cambio NO altera el comportamiento observable.

ALTER TABLE plans
    ADD COLUMN max_requests_per_minute INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN max_concurrent_slots INTEGER NOT NULL DEFAULT 0;

UPDATE plans SET max_requests_per_minute = 100,  max_concurrent_slots = 15  WHERE code = 'BASICO';
UPDATE plans SET max_requests_per_minute = 500,  max_concurrent_slots = 40  WHERE code = 'PRO';
UPDATE plans SET max_requests_per_minute = 2000, max_concurrent_slots = 100 WHERE code = 'ENTERPRISE';

-- A partir de aqui los valores son obligatorios: un plan sin limites de
-- trafico no es un plan valido, y su ausencia debe notarse al insertar.
ALTER TABLE plans ALTER COLUMN max_requests_per_minute DROP DEFAULT;
ALTER TABLE plans ALTER COLUMN max_concurrent_slots DROP DEFAULT;
