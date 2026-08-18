DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM OPS_CHANNEL_EXECUTION_POLICY
   WHERE operation_id <> '*' AND NOT REGEXP_LIKE(operation_id, '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$');
  IF v_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20922, 'CPF V122 rollback blocked: Channel policy contains Canonical operationId values that cannot map to legacy standardExecutionId');
  END IF;
END;
/
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT ck_ops_channel_execution_policy_operation;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY MODIFY operation_id VARCHAR2(10 CHAR);
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY RENAME COLUMN operation_id TO standard_execution_id;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT ck_cpf_channel_execution_policy_execution
  CHECK (standard_execution_id = '*' OR REGEXP_LIKE(standard_execution_id, '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'));
RENAME OPS_CHANNEL_POLICY_VERSION TO cpf_channel_policy_version;
RENAME OPS_CHANNEL_EXECUTION_POLICY TO cpf_channel_execution_policy;
RENAME OPS_CHANNEL_REGISTRY TO cpf_channel_registry;
