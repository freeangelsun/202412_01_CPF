-- RTF-038 reconciliation distributed claim rollback
DROP INDEX ix_cpf_unknown_result_claim ON cpf_unknown_result;
ALTER TABLE cpf_unknown_result
    DROP COLUMN row_version, DROP COLUMN lease_until, DROP COLUMN lease_owner,
    DROP COLUMN next_check_at, DROP COLUMN attempt_count;

DROP TABLE IF EXISTS cpf_cache_refresh_checkpoint;
DROP TABLE IF EXISTS cpf_runtime_change_audit;
DROP TABLE IF EXISTS cpf_runtime_delivery;
DROP TABLE IF EXISTS cpf_runtime_change;
DROP TABLE IF EXISTS cpf_control_operation;
DROP TABLE IF EXISTS cpf_runtime_instance_feature_state;
DROP TABLE IF EXISTS cpf_runtime_instance_state;
DROP TABLE IF EXISTS cpf_runtime_group_member;
DROP TABLE IF EXISTS cpf_runtime_instance_group;
DROP TABLE IF EXISTS cpf_runtime_rate_bucket;
DROP TABLE IF EXISTS cpf_runtime_controller_lease;
DROP TABLE IF EXISTS cpf_runtime_version;
DROP INDEX ix_cpf_service_instance_route ON cpf_service_instance;
DROP INDEX ix_cpf_service_instance_placement ON cpf_service_instance;
ALTER TABLE cpf_service_instance DROP CONSTRAINT ck_cpf_service_instance_drain;
ALTER TABLE cpf_service_instance DROP CONSTRAINT ck_cpf_service_instance_maintenance;
ALTER TABLE cpf_service_instance
  DROP COLUMN row_version, DROP COLUMN drain_deadline_at, DROP COLUMN drain_yn, DROP COLUMN maintenance_yn,
  DROP COLUMN priority_no, DROP COLUMN cell_code, DROP COLUMN zone_code, DROP COLUMN environment_code;
ALTER TABLE cpf_service_endpoint DROP COLUMN row_version;
ALTER TABLE cpf_service DROP COLUMN row_version;
