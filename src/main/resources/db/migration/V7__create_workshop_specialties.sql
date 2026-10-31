-- =============================================================================
-- ERP TENANT SERVICE - V7: CATÁLOGO DINÁMICO DE ESPECIALIDADES DE TALLER
-- =============================================================================
-- Permite que cada taller mantenga su propio catálogo de especialidades técnicas
-- para el personal operativo, con trazabilidad inmutable de revisiones.
-- =============================================================================

CREATE TABLE IF NOT EXISTS workshop_specialties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_workshop_specialties_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_workshop_specialties_tenant_active
    ON workshop_specialties(tenant_id, active);

CREATE INDEX IF NOT EXISTS idx_workshop_specialties_workshop
    ON workshop_specialties(workshop_id);

CREATE TABLE IF NOT EXISTS workshop_specialty_revisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES workshop_specialties(id) ON DELETE CASCADE,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_workshop_specialty_revisions_specialty
    ON workshop_specialty_revisions(tenant_id, specialty_id, changed_at);

-- Row-Level Security
ALTER TABLE workshop_specialties ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_specialty_revisions ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE policyname = 'workshop_specialty_tenant_isolation'
    ) THEN
        CREATE POLICY workshop_specialty_tenant_isolation ON workshop_specialties
            FOR ALL
            USING (
                nullif(current_setting('app.tenant_id', true), '') IS NULL
                OR tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid
            )
            WITH CHECK (
                nullif(current_setting('app.tenant_id', true), '') IS NULL
                OR tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE policyname = 'workshop_specialty_revision_tenant_isolation'
    ) THEN
        CREATE POLICY workshop_specialty_revision_tenant_isolation ON workshop_specialty_revisions
            FOR ALL
            USING (
                nullif(current_setting('app.tenant_id', true), '') IS NULL
                OR tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid
            )
            WITH CHECK (
                nullif(current_setting('app.tenant_id', true), '') IS NULL
                OR tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid
            );
    END IF;
END
$$;

-- Permisos para el rol de aplicación
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'erp_app') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE workshop_specialties TO erp_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE workshop_specialty_revisions TO erp_app;
    END IF;
END
$$;
