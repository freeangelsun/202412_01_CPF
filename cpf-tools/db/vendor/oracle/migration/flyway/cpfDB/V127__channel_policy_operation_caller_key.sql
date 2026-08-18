-- Canonical Channel Policy key: operationId + callerChannel.
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD (caller_channel VARCHAR2(30));
UPDATE OPS_CHANNEL_EXECUTION_POLICY SET caller_channel = caller_channel_code WHERE caller_channel IS NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY MODIFY (caller_channel NOT NULL);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT fk_cpf_channel_execution_policy_original;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT fk_cpf_channel_execution_policy_caller;
DROP INDEX ix_cpf_channel_execution_policy_lookup;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN original_channel_code;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN caller_channel_code;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN request_type;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT uk_ops_channel_execution_policy_operation_caller UNIQUE(operation_id, caller_channel);
CREATE INDEX ix_ops_channel_execution_policy_lookup ON OPS_CHANNEL_EXECUTION_POLICY(operation_id, caller_channel, active_yn);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_ops_channel_execution_policy_caller FOREIGN KEY (caller_channel) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
