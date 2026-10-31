-- =============================================================================
-- ERP TENANT SERVICE - V8: CORE BOUNDED CONTEXT (BRANCHES AND PROFILES)
-- =============================================================================
-- Supports multi-branch management, branch subscriptions, owner profiles,
-- employee profiles, and workshop updates with strict PostgreSQL RLS.
-- =============================================================================

-- 1. Extend tenants table with workshop configuration
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS owner_id UUID;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS mileage_interval_config INTEGER DEFAULT 5000;

-- 2. Owner Profiles table
CREATE TABLE IF NOT EXISTS owner_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_owner_profiles_user ON owner_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_owner_profiles_doc ON owner_profiles(document_number);

-- 3. Employee Profiles table
CREATE TABLE IF NOT EXISTS employee_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(20) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_employee_profiles_tenant_doc UNIQUE (tenant_id, document_number)
);

CREATE INDEX IF NOT EXISTS idx_employee_profiles_tenant_user ON employee_profiles(tenant_id, user_id);
CREATE INDEX IF NOT EXISTS idx_employee_profiles_doc ON employee_profiles(document_number);

-- 4. Branches table
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_branches_workshop_code UNIQUE (workshop_id, code)
);

CREATE INDEX IF NOT EXISTS idx_branches_workshop ON branches(workshop_id);
CREATE INDEX IF NOT EXISTS idx_branches_tenant_active ON branches(tenant_id, active);

-- 5. Branch Subscriptions table
CREATE TABLE IF NOT EXISTS branch_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id),
    billing_cycle VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_branch_subscriptions_branch_status ON branch_subscriptions(branch_id, status);

-- 6. Row-Level Security
ALTER TABLE owner_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE employee_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE branch_subscriptions ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE policyname = 'owner_profile_tenant_isolation'
    ) THEN
        CREATE POLICY owner_profile_tenant_isolation ON owner_profiles
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
        SELECT 1 FROM pg_policies WHERE policyname = 'employee_profile_tenant_isolation'
    ) THEN
        CREATE POLICY employee_profile_tenant_isolation ON employee_profiles
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
        SELECT 1 FROM pg_policies WHERE policyname = 'branch_tenant_isolation'
    ) THEN
        CREATE POLICY branch_tenant_isolation ON branches
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
        SELECT 1 FROM pg_policies WHERE policyname = 'branch_subscription_tenant_isolation'
    ) THEN
        CREATE POLICY branch_subscription_tenant_isolation ON branch_subscriptions
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

-- 7. Database grants for application role
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'erp_app') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE owner_profiles TO erp_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE employee_profiles TO erp_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE branches TO erp_app;
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE branch_subscriptions TO erp_app;
    END IF;
END
$$;
