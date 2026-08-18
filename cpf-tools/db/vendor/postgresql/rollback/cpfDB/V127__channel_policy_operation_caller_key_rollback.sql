-- Lossy semantic rollback: legacy original channel/request type dimensions are reconstructed from callerChannel/ONLINE.
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD COLUMN original_channel_code VARCHAR(30), ADD COLUMN caller_channel_code VARCHAR(30), ADD COLUMN request_type VARCHAR(30) NOT NULL DEFAULT 'ONLINE';
UPDATE OPS_CHANNEL_EXECUTION_POLICY SET original_channel_code=caller_channel, caller_channel_code=caller_channel;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ALTER COLUMN original_channel_code SET NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ALTER COLUMN caller_channel_code SET NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT IF EXISTS fk_ops_channel_execution_policy_caller;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT IF EXISTS uk_ops_channel_execution_policy_operation_caller;
DROP INDEX IF EXISTS ix_ops_channel_execution_policy_lookup;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP COLUMN caller_channel;
CREATE INDEX ix_cpf_channel_execution_policy_lookup ON OPS_CHANNEL_EXECUTION_POLICY(operation_id, original_channel_code, caller_channel_code, request_type, active_yn);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_cpf_channel_execution_policy_original FOREIGN KEY (original_channel_code) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT fk_cpf_channel_execution_policy_caller FOREIGN KEY (caller_channel_code) REFERENCES OPS_CHANNEL_REGISTRY(channel_code);
