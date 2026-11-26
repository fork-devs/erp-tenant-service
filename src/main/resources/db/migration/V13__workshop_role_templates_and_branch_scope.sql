ALTER TABLE workshop_roles
    ADD COLUMN IF NOT EXISTS template_code VARCHAR(60);

ALTER TABLE workshop_role_assignments
    ADD COLUMN IF NOT EXISTS branch_id UUID;

CREATE INDEX IF NOT EXISTS ix_workshop_role_assignments_branch
    ON workshop_role_assignments (tenant_id, user_id, branch_id);
