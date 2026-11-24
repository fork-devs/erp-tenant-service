-- =============================================================================
-- V10: Custom Workshop Roles, Role Assignments, Audit Logs & Profile Image URL
-- =============================================================================

ALTER TABLE owner_profiles
    ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

ALTER TABLE employee_profiles
    ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

CREATE TABLE IF NOT EXISTS workshop_roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    workshop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    permissions TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workshop_roles_workshop_name UNIQUE (workshop_id, name)
);

CREATE TABLE IF NOT EXISTS workshop_role_assignments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    workshop_id UUID NOT NULL,
    workshop_role_id UUID NOT NULL REFERENCES workshop_roles(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workshop_role_assignments_user UNIQUE (workshop_id, workshop_role_id, user_id)
);

CREATE TABLE IF NOT EXISTS workshop_role_audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    workshop_id UUID NOT NULL,
    workshop_role_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    target_user_id UUID,
    action_type VARCHAR(30) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    permissions_snapshot TEXT NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

ALTER TABLE workshop_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_roles FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_workshop_roles ON workshop_roles
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

ALTER TABLE workshop_role_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_role_assignments FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_workshop_role_assignments ON workshop_role_assignments
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

ALTER TABLE workshop_role_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_role_audit_logs FORCE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_workshop_role_audit_logs ON workshop_role_audit_logs
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant_id', true)::uuid);
