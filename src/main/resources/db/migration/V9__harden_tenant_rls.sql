-- =============================================================================
-- V9: Harden PostgreSQL Row-Level Security (Fail-Closed + FORCE RLS)
-- =============================================================================

-- 1. Subscriptions: enable + force strict tenant isolation
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE subscriptions FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS subscription_tenant_isolation ON subscriptions;
CREATE POLICY subscription_tenant_isolation ON subscriptions
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- 2. Owner profiles: replace fail-open policy with strict fail-closed + FORCE RLS
ALTER TABLE owner_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE owner_profiles FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS owner_profile_tenant_isolation ON owner_profiles;
CREATE POLICY owner_profile_tenant_isolation ON owner_profiles
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- 3. Employee profiles: replace fail-open policy with strict fail-closed + FORCE RLS
ALTER TABLE employee_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE employee_profiles FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS employee_profile_tenant_isolation ON employee_profiles;
CREATE POLICY employee_profile_tenant_isolation ON employee_profiles
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- 4. Branches: replace fail-open policy with strict fail-closed + FORCE RLS
ALTER TABLE branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE branches FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS branch_tenant_isolation ON branches;
CREATE POLICY branch_tenant_isolation ON branches
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- 5. Branch subscriptions: replace fail-open policy with strict fail-closed + FORCE RLS
ALTER TABLE branch_subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE branch_subscriptions FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS branch_subscription_tenant_isolation ON branch_subscriptions;
CREATE POLICY branch_subscription_tenant_isolation ON branch_subscriptions
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

-- 6. Workshop specialties & revisions: replace fail-open policies with strict fail-closed + FORCE RLS
ALTER TABLE workshop_specialties ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_specialties FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS workshop_specialties_tenant_isolation ON workshop_specialties;
CREATE POLICY workshop_specialties_tenant_isolation ON workshop_specialties
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER TABLE workshop_specialty_revisions ENABLE ROW LEVEL SECURITY;
ALTER TABLE workshop_specialty_revisions FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS workshop_specialty_revisions_tenant_isolation ON workshop_specialty_revisions;
CREATE POLICY workshop_specialty_revisions_tenant_isolation ON workshop_specialty_revisions
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);
