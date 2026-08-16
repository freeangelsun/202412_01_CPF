-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.
-- D-009 rollback: one transaction, all-target preflight before writes.
BEGIN;
CREATE TEMPORARY TABLE cpf_bat_runtime_role_guard (invalid_count BIGINT NOT NULL CHECK (invalid_count = 0)) ON COMMIT DROP;
INSERT INTO cpf_bat_runtime_role_guard (invalid_count) SELECT (SELECT COUNT(*) FROM (SELECT runtime_role AS runtime_role FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_DEPLOYMENT_CELL) role_values WHERE runtime_role IS NOT NULL AND runtime_role NOT IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT', 'CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT')) + ABS(3 - (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = current_schema() AND constraint_type = 'CHECK' AND ((lower(table_name) = lower('OPS_RUNTIME_INSTANCE_STATE') AND constraint_name = 'ck_ops_runtime_instance_role') OR (lower(table_name) = lower('BAT_RUNTIME_INSTANCE') AND constraint_name = 'ck_bat_runtime_instance_role') OR (lower(table_name) = lower('BAT_DEPLOYMENT_CELL') AND constraint_name = 'ck_bat_deployment_runtime_role'))));
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE BAT_RUNTIME_INSTANCE DROP CONSTRAINT ck_bat_runtime_instance_role;
ALTER TABLE BAT_DEPLOYMENT_CELL DROP CONSTRAINT ck_bat_deployment_runtime_role;
UPDATE OPS_RUNTIME_INSTANCE_STATE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
UPDATE BAT_RUNTIME_INSTANCE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
UPDATE BAT_DEPLOYMENT_CELL SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
COMMIT;
