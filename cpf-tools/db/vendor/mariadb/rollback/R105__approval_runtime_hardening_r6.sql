DROP TRIGGER IF EXISTS tr_adm_approval_policy_no_overlap;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_u;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_d;
DROP TABLE IF EXISTS adm_approval_capability_nonce;
DROP TABLE IF EXISTS adm_approval_policy_lock;
DROP INDEX IF EXISTS ix_adm_approval_execution_lease ON adm_approval_execution;
ALTER TABLE adm_approval_execution DROP COLUMN IF EXISTS FENCE_TOKEN;
ALTER TABLE adm_approval_execution DROP COLUMN IF EXISTS LEASE_EXPIRES_AT;
ALTER TABLE adm_approval_execution DROP COLUMN IF EXISTS LEASE_OWNER;
