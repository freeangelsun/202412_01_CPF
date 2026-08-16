-- GENERATED FILE. DO NOT EDIT VENDOR SQL DIRECTLY.
-- Source: cpf-tools/db/metadata/platform-nullable-empty-string-repair.json + canonical schemaVersion 46.
-- Repair: CPF-MIGRATION-096-NULLABLE-EMPTY-STRING; historical migrations remain immutable.

UPDATE cpf_cache_invalidation_event SET cache_key = '' WHERE cache_key IS NULL;
ALTER TABLE cpf_cache_invalidation_event
    MODIFY COLUMN cache_key VARCHAR(512) NOT NULL DEFAULT '';

UPDATE cpf_gateway_apply_status SET applied_version = '' WHERE applied_version IS NULL;
UPDATE cpf_gateway_apply_status SET error_code = '' WHERE error_code IS NULL;
UPDATE cpf_gateway_apply_status SET error_message = '' WHERE error_message IS NULL;
ALTER TABLE cpf_gateway_apply_status
    MODIFY COLUMN applied_version VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN error_code VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN error_message VARCHAR(1000) NOT NULL DEFAULT '';

UPDATE cpf_gateway_attempt SET target_host = '' WHERE target_host IS NULL;
UPDATE cpf_gateway_attempt SET protocol_status = '' WHERE protocol_status IS NULL;
UPDATE cpf_gateway_attempt SET failure_code = '' WHERE failure_code IS NULL;
UPDATE cpf_gateway_attempt SET failure_message = '' WHERE failure_message IS NULL;
ALTER TABLE cpf_gateway_attempt
    MODIFY COLUMN target_host VARCHAR(300) NOT NULL DEFAULT '',
    MODIFY COLUMN protocol_status VARCHAR(30) NOT NULL DEFAULT '',
    MODIFY COLUMN failure_code VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN failure_message VARCHAR(1000) NOT NULL DEFAULT '';

UPDATE cpf_gateway_binding SET tls_policy_id = '' WHERE tls_policy_id IS NULL;
UPDATE cpf_gateway_binding SET authentication_policy_id = '' WHERE authentication_policy_id IS NULL;
UPDATE cpf_gateway_binding SET authorization_policy_id = '' WHERE authorization_policy_id IS NULL;
UPDATE cpf_gateway_binding SET header_policy_id = '' WHERE header_policy_id IS NULL;
UPDATE cpf_gateway_binding SET rate_limit_policy_id = '' WHERE rate_limit_policy_id IS NULL;
UPDATE cpf_gateway_binding SET health_policy_id = '' WHERE health_policy_id IS NULL;
UPDATE cpf_gateway_binding SET failover_group_id = '' WHERE failover_group_id IS NULL;
UPDATE cpf_gateway_binding SET approval_id = '' WHERE approval_id IS NULL;
ALTER TABLE cpf_gateway_binding
    MODIFY COLUMN tls_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN authentication_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN authorization_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN header_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN rate_limit_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN health_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN failover_group_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN approval_id VARCHAR(100) NOT NULL DEFAULT '';

UPDATE cpf_gateway_connection_test SET gateway_instance_id = '' WHERE gateway_instance_id IS NULL;
UPDATE cpf_gateway_connection_test SET instance_id = '' WHERE instance_id IS NULL;
UPDATE cpf_gateway_connection_test SET failure_stage = '' WHERE failure_stage IS NULL;
UPDATE cpf_gateway_connection_test SET trace_id = '' WHERE trace_id IS NULL;
UPDATE cpf_gateway_connection_test SET operation_id = '' WHERE operation_id IS NULL;
ALTER TABLE cpf_gateway_connection_test
    MODIFY COLUMN gateway_instance_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN instance_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN failure_stage VARCHAR(50) NOT NULL DEFAULT '',
    MODIFY COLUMN trace_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN operation_id VARCHAR(100) NOT NULL DEFAULT '';

UPDATE cpf_gateway_server_group SET hash_key_source = '' WHERE hash_key_source IS NULL;
UPDATE cpf_gateway_server_group SET health_policy_id = '' WHERE health_policy_id IS NULL;
UPDATE cpf_gateway_server_group SET failover_group_id = '' WHERE failover_group_id IS NULL;
ALTER TABLE cpf_gateway_server_group
    MODIFY COLUMN hash_key_source VARCHAR(200) NOT NULL DEFAULT '',
    MODIFY COLUMN health_policy_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN failover_group_id VARCHAR(100) NOT NULL DEFAULT '';

UPDATE cpf_gateway_transaction SET channel_id = '' WHERE channel_id IS NULL;
UPDATE cpf_gateway_transaction SET source_ip = '' WHERE source_ip IS NULL;
UPDATE cpf_gateway_transaction SET final_instance_id = '' WHERE final_instance_id IS NULL;
UPDATE cpf_gateway_transaction SET protocol_status = '' WHERE protocol_status IS NULL;
UPDATE cpf_gateway_transaction SET business_code = '' WHERE business_code IS NULL;
UPDATE cpf_gateway_transaction SET failure_stage = '' WHERE failure_stage IS NULL;
ALTER TABLE cpf_gateway_transaction
    MODIFY COLUMN channel_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN source_ip VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN final_instance_id VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN protocol_status VARCHAR(30) NOT NULL DEFAULT '',
    MODIFY COLUMN business_code VARCHAR(100) NOT NULL DEFAULT '',
    MODIFY COLUMN failure_stage VARCHAR(50) NOT NULL DEFAULT '';
