-- RTF-038 reconciliation distributed claim rollback
DROP INDEX IF EXISTS ix_cpf_unknown_result_claim;
ALTER TABLE cpf_unknown_result
    DROP COLUMN IF EXISTS row_version, DROP COLUMN IF EXISTS lease_until, DROP COLUMN IF EXISTS lease_owner,
    DROP COLUMN IF EXISTS next_check_at, DROP COLUMN IF EXISTS attempt_count;

DROP TABLE IF EXISTS cpf_cache_refresh_checkpoint; DROP TABLE IF EXISTS cpf_runtime_change_audit; DROP TABLE IF EXISTS cpf_runtime_delivery; DROP TABLE IF EXISTS cpf_runtime_change; DROP TABLE IF EXISTS cpf_control_operation; DROP TABLE IF EXISTS cpf_runtime_instance_feature_state;
DROP TABLE IF EXISTS cpf_runtime_instance_state; DROP TABLE IF EXISTS cpf_runtime_group_member; DROP TABLE IF EXISTS cpf_runtime_instance_group; DROP TABLE IF EXISTS cpf_runtime_rate_bucket;
DROP TABLE IF EXISTS cpf_runtime_controller_lease;
DROP TABLE IF EXISTS cpf_runtime_version;
DROP INDEX IF EXISTS ix_cpf_service_instance_route; DROP INDEX IF EXISTS ix_cpf_service_instance_placement;
ALTER TABLE cpf_service_instance DROP CONSTRAINT IF EXISTS ck_cpf_service_instance_drain; ALTER TABLE cpf_service_instance DROP CONSTRAINT IF EXISTS ck_cpf_service_instance_maintenance;
ALTER TABLE cpf_service_instance DROP COLUMN IF EXISTS row_version, DROP COLUMN IF EXISTS drain_deadline_at, DROP COLUMN IF EXISTS drain_yn, DROP COLUMN IF EXISTS maintenance_yn, DROP COLUMN IF EXISTS priority_no, DROP COLUMN IF EXISTS cell_code, DROP COLUMN IF EXISTS zone_code, DROP COLUMN IF EXISTS environment_code;
ALTER TABLE cpf_service_endpoint DROP COLUMN IF EXISTS row_version;
ALTER TABLE cpf_service DROP COLUMN IF EXISTS row_version;
