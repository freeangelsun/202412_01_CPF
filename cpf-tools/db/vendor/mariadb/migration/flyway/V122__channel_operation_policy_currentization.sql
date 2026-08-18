-- CPF V122: align Channel policy with Canonical business operationId and OPS_ physical ownership.
RENAME TABLE cpf_channel_registry TO OPS_CHANNEL_REGISTRY,
             cpf_channel_execution_policy TO OPS_CHANNEL_EXECUTION_POLICY,
             cpf_channel_policy_version TO OPS_CHANNEL_POLICY_VERSION;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT ck_cpf_channel_execution_policy_execution;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY CHANGE COLUMN standard_execution_id operation_id VARCHAR(160) NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT ck_ops_channel_execution_policy_operation
  CHECK (operation_id = '*' OR operation_id REGEXP '^[A-Za-z][A-Za-z0-9_.:-]{2,159}$');
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY COMMENT = 'CPF 업무 Operation별 최초·호출 채널 정책';
