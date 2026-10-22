-- =============================================================================
-- ERP TENANT SERVICE - V4: ROL DE APLICACION Y PERMISOS
-- =============================================================================
-- La aplicacion conectaba con el rol propietario de las tablas. Se separa el
-- rol de ejecucion del rol de migracion, de modo que el servicio no acumula
-- permisos de DDL y el aislamiento por taller pueda apoyarse en el dueno de las
-- tablas en lugar de depender solo del codigo.
--
-- Ver docs/doc-estado-implementacion.md, hallazgos P1 6.6 y P1 6.14.
-- =============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'erp_app') THEN
        EXECUTE format(
            'CREATE ROLE erp_app LOGIN NOINHERIT NOSUPERUSER NOCREATEDB '
            'NOCREATEROLE NOREPLICATION NOBYPASSRLS PASSWORD %L',
            '${appRolePassword}'
        );
    END IF;
END
$$;

-- Sin USAGE sobre el esquema, el rol no resuelve ningun objeto aunque tenga
-- privilegios sobre las tablas.
GRANT USAGE ON SCHEMA public TO erp_app;

-- El tenant es la fila: la tabla tenants se indexa por su identificador, que es
-- el propio identificador de taller.
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tenants TO erp_app;

-- Catalogo global de planes, compartido por todos los talleres: solo lectura.
GRANT SELECT ON TABLE plans TO erp_app;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE subscriptions TO erp_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE tenant_outbox TO erp_app;

-- Comprobacion de que el rol no puede eludir la politica de aislamiento.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_roles
        WHERE rolname = 'erp_app' AND rolbypassrls
    ) THEN
        RAISE EXCEPTION 'El rol erp_app no debe tener BYPASSRLS: la politica de aislamiento seria eludible';
    END IF;
END
$$;
