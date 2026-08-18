DROP PROCEDURE IF EXISTS CPF_V122_ROLLBACK_GUARD;
DELIMITER $$
CREATE PROCEDURE CPF_V122_ROLLBACK_GUARD()
BEGIN
  IF EXISTS (SELECT 1 FROM OPS_CHANNEL_EXECUTION_POLICY WHERE operation_id <> '*' AND operation_id NOT REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='CPF V122 rollback blocked: Channel policy contains Canonical operationId values that cannot map to legacy standardExecutionId';
  END IF;
END$$
DELIMITER ;
CALL CPF_V122_ROLLBACK_GUARD();
DROP PROCEDURE CPF_V122_ROLLBACK_GUARD;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY DROP CONSTRAINT ck_ops_channel_execution_policy_operation;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY CHANGE COLUMN operation_id standard_execution_id VARCHAR(10) NOT NULL;
ALTER TABLE OPS_CHANNEL_EXECUTION_POLICY ADD CONSTRAINT ck_cpf_channel_execution_policy_execution
  CHECK (standard_execution_id = '*' OR standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$');
RENAME TABLE OPS_CHANNEL_POLICY_VERSION TO cpf_channel_policy_version,
             OPS_CHANNEL_EXECUTION_POLICY TO cpf_channel_execution_policy,
             OPS_CHANNEL_REGISTRY TO cpf_channel_registry;
