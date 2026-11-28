-- =============================================================================
-- V15: Workshop & Branch Operating Hours and Holiday Exceptions
-- =============================================================================

CREATE TABLE IF NOT EXISTS workshop_operating_hours (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    workshop_id UUID NOT NULL,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workshop_operating_hours UNIQUE (workshop_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS branch_operating_hours (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_branch_operating_hours UNIQUE (branch_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS operating_hour_exceptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    scope_type VARCHAR(20) NOT NULL CHECK (scope_type IN ('WORKSHOP', 'BRANCH')),
    scope_id UUID NOT NULL,
    exception_date DATE NOT NULL,
    closed BOOLEAN NOT NULL DEFAULT TRUE,
    open_time TIME,
    close_time TIME,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_operating_hour_exception UNIQUE (scope_type, scope_id, exception_date)
);

CREATE INDEX IF NOT EXISTS ix_operating_hour_exceptions_scope_date
    ON operating_hour_exceptions (scope_type, scope_id, exception_date);

ALTER TABLE workshop_operating_hours ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_operating_hours FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_workshop_operating_hours ON workshop_operating_hours
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

ALTER TABLE branch_operating_hours ENABLE ROW LEVEL SECURITY;
ALTER TABLE branch_operating_hours FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_branch_operating_hours ON branch_operating_hours
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

ALTER TABLE operating_hour_exceptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE operating_hour_exceptions FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_operating_hour_exceptions ON operating_hour_exceptions
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
