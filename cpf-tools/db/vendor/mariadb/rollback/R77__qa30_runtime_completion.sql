DROP TABLE IF EXISTS cpf_gateway_transaction_capture_segment;
-- QA30-WP07 rollback: Versioned Log Capture Policy Schema V2
ALTER TABLE cpf_log_policy_override DROP COLUMN policy_checksum;
ALTER TABLE cpf_log_policy_override DROP COLUMN max_stack_bytes;
ALTER TABLE cpf_log_policy_override DROP COLUMN max_response_body_bytes;
ALTER TABLE cpf_log_policy_override DROP COLUMN max_request_body_bytes;
ALTER TABLE cpf_log_policy_override DROP COLUMN max_header_bytes;
ALTER TABLE cpf_log_policy_override DROP COLUMN max_query_bytes;
ALTER TABLE cpf_log_policy_override DROP COLUMN field_allowlist;
ALTER TABLE cpf_log_policy_override DROP COLUMN header_allowlist;
ALTER TABLE cpf_log_policy_override DROP COLUMN query_allowlist;
ALTER TABLE cpf_log_policy_override DROP COLUMN error_stack_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN response_body_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN request_body_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN response_header_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN request_header_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN query_capture_mode;
ALTER TABLE cpf_log_policy_override DROP COLUMN policy_schema_version;
ALTER TABLE cpf_log_policy DROP COLUMN policy_checksum;
ALTER TABLE cpf_log_policy DROP COLUMN max_stack_bytes;
ALTER TABLE cpf_log_policy DROP COLUMN max_response_body_bytes;
ALTER TABLE cpf_log_policy DROP COLUMN max_request_body_bytes;
ALTER TABLE cpf_log_policy DROP COLUMN max_header_bytes;
ALTER TABLE cpf_log_policy DROP COLUMN max_query_bytes;
ALTER TABLE cpf_log_policy DROP COLUMN field_allowlist;
ALTER TABLE cpf_log_policy DROP COLUMN header_allowlist;
ALTER TABLE cpf_log_policy DROP COLUMN query_allowlist;
ALTER TABLE cpf_log_policy DROP COLUMN error_stack_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN response_body_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN request_body_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN response_header_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN request_header_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN query_capture_mode;
ALTER TABLE cpf_log_policy DROP COLUMN policy_schema_version;

-- CPF QA30 additive rollback.

USE cpfDB;
-- CPF_LOGICAL_DATABASE=cpfDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.
DROP TABLE cpf_gateway_health_history;
DROP TABLE cpf_gateway_connection_test_operation;
DROP TABLE cpf_gateway_spool_checkpoint;
DROP TABLE cpf_gateway_operation_idempotency;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN active_requests;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN last_probe_code;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN ewma_latency_ms;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN last_probe_at;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN probe_lease_until;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN consecutive_failures;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN consecutive_successes;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN probe_owner_id;
ALTER TABLE cpf_gateway_server_group_member DROP COLUMN canary_percent;
ALTER TABLE cpf_gateway_attempt DROP COLUMN gateway_instance_id;
ALTER TABLE cpf_gateway_attempt DROP COLUMN unknown_yn;
ALTER TABLE cpf_gateway_attempt DROP COLUMN selection_reason;
ALTER TABLE cpf_gateway_binding DROP COLUMN retired_at;
ALTER TABLE cpf_gateway_binding DROP COLUMN retired_by;
ALTER TABLE cpf_gateway_binding DROP COLUMN binding_checksum;
ALTER TABLE cpf_gateway_transaction DROP COLUMN completed_at;
ALTER TABLE cpf_gateway_transaction DROP COLUMN request_method;
ALTER TABLE cpf_gateway_transaction DROP COLUMN request_path;
ALTER TABLE cpf_gateway_transaction DROP COLUMN config_checksum;
ALTER TABLE cpf_gateway_transaction DROP COLUMN binding_version;


USE cmnDB;
-- CPF_LOGICAL_DATABASE=cmnDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.


USE batDB;
-- CPF_LOGICAL_DATABASE=batDB
-- Rollback removes only V77 additive artifacts; safety relaxations remain compatible.
DROP TABLE bat_execution_result_detail;
DROP TABLE bat_job_runtime_projection_outbox;
DROP TABLE bat_job_runtime_projection;
ALTER TABLE bat_job DROP FOREIGN KEY fk_bat_job_published_definition;
ALTER TABLE bat_execution DROP COLUMN definition_checksum, DROP COLUMN definition_version;
ALTER TABLE bat_schedule DROP COLUMN definition_checksum, DROP COLUMN definition_version;
ALTER TABLE bat_job DROP COLUMN definition_published_at, DROP COLUMN executor_reference, DROP COLUMN published_definition_checksum, DROP COLUMN published_definition_version;
