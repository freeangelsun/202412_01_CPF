-- CPF V122: align Channel policy with Canonical business operationId and OPS_ physical ownership.
ALTER TABLE cpf_channel_registry RENAME TO OPS_CHANNEL_REGISTRY;
ALTER TABLE cpf_channel_execution_policy RENAME TO OPS_CHANNEL_EXECUTION_POLICY;
ALTER TABLE cpf_channel_policy_version RENAME TO OPS_CHANNEL_POLICY_VERSION;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY RENAME COLUMN standard_execution_id TO operation_id;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ALTER COLUMN operation_id TYPE VARCHAR(160);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT ck_cpf_channel_execution_policy_execution;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT ck_ops_channel_execution_policy_operation
  CHECK (operation_id = '*' OR operation_id ~ '^[A-Za-z][A-Za-z0-9_.:-]{2,159}$');
COMMENT ON TABLE OPS_CHANNEL_EXECUTION_POLICY IS 'CPF 업무 Operation별 최초·호출 채널 정책';
COMMENT ON COLUMN OPS_CHANNEL_EXECUTION_POLICY.operation_id IS 'Canonical 업무 operationId 또는 전체 Operation *';
