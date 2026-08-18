-- Lossy semantic rollback: legacy original channel/request type dimensions are reconstructed from callerChannel/ONLINE.
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD COLUMN original_channel_code VARCHAR(30) NULL AFTER operation_id, ADD COLUMN caller_channel_code VARCHAR(30) NULL AFTER original_channel_code, ADD COLUMN request_type VARCHAR(30) NOT NULL DEFAULT 'ONLINE' AFTER caller_channel_code;
UPDATE OPS_CHANNEL_EXECUTION_POLICY SET original_channel_code=caller_channel, caller_channel_code=caller_channel WHERE original_channel_code IS NULL OR caller_channel_code IS NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY MODIFY original_channel_code VARCHAR(30) NOT NULL, MODIFY caller_channel_code VARCHAR(30) NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP FOREIGN KEY fk_ops_channel_execution_policy_caller;
DROP INDEX uk_ops_channel_execution_policy_operation_caller ON OPS_CHANNEL_EXECUTION_POLICY;
DROP INDEX ix_ops_channel_execution_policy_lookup ON OPS_CHANNEL_EXECUTION_POLICY;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN caller_channel;
CREATE INDEX ix_cpf_channel_execution_policy_lookup ON OPS_CHANNEL_EXECUTION_POLICY(operation_id, original_channel_code, caller_channel_code, request_type, active_yn);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_cpf_channel_execution_policy_original FOREIGN KEY (original_channel_code) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_cpf_channel_execution_policy_caller FOREIGN KEY (caller_channel_code) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
