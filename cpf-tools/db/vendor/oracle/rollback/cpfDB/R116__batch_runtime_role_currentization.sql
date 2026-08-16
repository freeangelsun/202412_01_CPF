-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
-- D-009 rollback: phase 1 validates every target before any write or DDL.
DECLARE
  v_invalid_count NUMBER;
BEGIN
  SELECT (SELECT COUNT(*) FROM (SELECT runtime_role AS runtime_role FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_DEPLOYMENT_CELL) role_values WHERE runtime_role IS NOT NULL AND runtime_role NOT IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT', 'CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT')) + ABS(3 - (SELECT COUNT(*) FROM user_constraints WHERE constraint_type = 'C' AND ((table_name = UPPER('OPS_RUNTIME_INSTANCE_STATE') AND constraint_name = UPPER('ck_ops_runtime_instance_role')) OR (table_name = UPPER('BAT_RUNTIME_INSTANCE') AND constraint_name = UPPER('ck_bat_runtime_instance_role')) OR (table_name = UPPER('BAT_DEPLOYMENT_CELL') AND constraint_name = UPPER('ck_bat_deployment_runtime_role'))))) INTO v_invalid_count FROM dual;
  IF v_invalid_count <> 0 THEN
    raise_application_error(-20009, 'D-009 runtime role preflight failed');
  END IF;
EXCEPTION WHEN OTHERS THEN
  ROLLBACK;
  RAISE;
END;
/
-- D-009 rollback: phase 2 applies the data operation and constraint lifecycle.
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE BAT_RUNTIME_INSTANCE DROP CONSTRAINT ck_bat_runtime_instance_role;
ALTER TABLE BAT_DEPLOYMENT_CELL DROP CONSTRAINT ck_bat_deployment_runtime_role;
BEGIN
  UPDATE OPS_RUNTIME_INSTANCE_STATE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
  UPDATE BAT_RUNTIME_INSTANCE SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
  UPDATE BAT_DEPLOYMENT_CELL SET runtime_role = CASE runtime_role WHEN 'CONTROL_PLANE' THEN 'CONTROL_SERVER' WHEN 'CENTER_CUT' THEN 'CENTER_CUT_RUNNER' WHEN 'AGENT' THEN 'HOST_AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_PLANE', 'CENTER_CUT', 'AGENT');
EXCEPTION WHEN OTHERS THEN
  ROLLBACK;
  RAISE;
END;
/
COMMIT;
