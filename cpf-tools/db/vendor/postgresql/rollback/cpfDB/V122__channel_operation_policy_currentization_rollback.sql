DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM OPS_CHANNEL_EXECUTION_POLICY WHERE operation_id <> '*' AND operation_id !~ '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$') THEN
    RAISE EXCEPTION 'CPF V122 rollback blocked: Channel policy contains Canonical operationId values that cannot map to legacy standardExecutionId';
  END IF;
END $$;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT ck_ops_channel_execution_policy_operation;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ALTER COLUMN operation_id TYPE VARCHAR(10);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY RENAME COLUMN operation_id TO standard_execution_id;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT ck_cpf_channel_execution_policy_execution
  CHECK (standard_execution_id = '*' OR standard_execution_id ~ '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$');
ALTER TABLE OPS_CHANNEL_POLICY_VERSION RENAME TO cpf_channel_policy_version;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY RENAME TO cpf_channel_execution_policy;
ALTER TABLE OPS_CHANNEL_REGISTRY RENAME TO cpf_channel_registry;
