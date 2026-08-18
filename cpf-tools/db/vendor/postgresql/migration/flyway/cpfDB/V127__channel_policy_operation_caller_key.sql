-- Canonical Channel Policy key: operationId + callerChannel.
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD COLUMN caller_channel VARCHAR(30);
UPDATE OPS_CHANNEL_EXECUTION_POLICY SET caller_channel = caller_channel_code WHERE caller_channel IS NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ALTER COLUMN caller_channel SET NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT IF EXISTS fk_cpf_channel_execution_policy_original;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT IF EXISTS fk_cpf_channel_execution_policy_caller;
DROP INDEX IF EXISTS ix_cpf_channel_execution_policy_lookup;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN original_channel_code, DROP COLUMN caller_channel_code, DROP COLUMN request_type;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT uk_ops_channel_execution_policy_operation_caller UNIQUE(operation_id, caller_channel);
CREATE INDEX ix_ops_channel_execution_policy_lookup ON OPS_CHANNEL_EXECUTION_POLICY(operation_id, caller_channel, active_yn);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_ops_channel_execution_policy_caller FOREIGN KEY (caller_channel) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
