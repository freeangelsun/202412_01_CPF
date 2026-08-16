-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.
-- D-009 rollback: phase 1 validates every target before any write.
USE cpfDB;
DROP TEMPORARY TABLE IF EXISTS cpf_bat_runtime_role_guard;
CREATE TEMPORARY TABLE cpf_bat_runtime_role_guard (invalid_count BIGINT NOT NULL, CONSTRAINT ck_cpf_bat_runtime_role_guard CHECK (invalid_count = 0));
INSERT INTO cpf_bat_runtime_role_guard (invalid_count) SELECT (SELECT COUNT(*) FROM (SELECT runtime_role AS runtime_role FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_DEPLOYMENT_CELL) role_values WHERE runtime_role IS NOT NULL AND BINARY runtime_role NOT IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT', 'CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT')) + ABS(3 - (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_TYPE = 'CHECK' AND ((LOWER(TABLE_NAME) = LOWER('OPS_RUNTIME_INSTANCE_STATE') AND CONSTRAINT_NAME = 'ck_ops_runtime_instance_role') OR (LOWER(TABLE_NAME) = LOWER('BAT_RUNTIME_INSTANCE') AND CONSTRAINT_NAME = 'ck_bat_runtime_instance_role') OR (LOWER(TABLE_NAME) = LOWER('BAT_DEPLOYMENT_CELL') AND CONSTRAINT_NAME = 'ck_bat_deployment_runtime_role'))));
DROP TEMPORARY TABLE cpf_bat_runtime_role_guard;
-- D-009 rollback: phase 2 applies the data operation transaction and constraint lifecycle.
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE BAT_RUNTIME_INSTANCE DROP CONSTRAINT ck_bat_runtime_instance_role;
ALTER TABLE BAT_DEPLOYMENT_CELL DROP CONSTRAINT ck_bat_deployment_runtime_role;
START TRANSACTION;
UPDATE OPS_RUNTIME_INSTANCE_STATE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE BINARY runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
UPDATE BAT_RUNTIME_INSTANCE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE BINARY runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
UPDATE BAT_DEPLOYMENT_CELL SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE BINARY runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
COMMIT;
