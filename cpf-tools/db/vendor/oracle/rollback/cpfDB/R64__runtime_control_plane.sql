-- RTF-038 reconciliation distributed claim rollback
DROP INDEX ix_cpf_unknown_result_claim;
ALTER TABLE cpf_unknown_result DROP (row_version, lease_until, lease_owner, next_check_at, attempt_count);

DROP TABLE cpf_cache_refresh_checkpoint CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_change_audit CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_delivery CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_change CASCADE CONSTRAINTS; DROP TABLE cpf_control_operation CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_instance_feature_state CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_instance_state CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_group_member CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_instance_group CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_rate_bucket CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_controller_lease CASCADE CONSTRAINTS; DROP TABLE cpf_runtime_version CASCADE CONSTRAINTS;
DROP INDEX ix_cpf_service_instance_route; DROP INDEX ix_cpf_service_instance_placement;
ALTER TABLE cpf_service_instance DROP CONSTRAINT ck_cpf_service_instance_drain; ALTER TABLE cpf_service_instance DROP CONSTRAINT ck_cpf_service_instance_maintenance;
ALTER TABLE cpf_service_instance DROP (row_version,drain_deadline_at,drain_yn,maintenance_yn,priority_no,cell_code,zone_code,environment_code);
ALTER TABLE cpf_service_endpoint DROP (row_version);
ALTER TABLE cpf_service DROP (row_version);
