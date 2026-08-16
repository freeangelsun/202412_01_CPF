-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
-- D-009 forward: phase 1 validates every target before any write or DDL.
DECLARE
  v_invalid_count NUMBER;
BEGIN
  SELECT (SELECT COUNT(*) FROM (SELECT runtime_role AS runtime_role FROM OPS_RUNTIME_INSTANCE_STATE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_RUNTIME_INSTANCE UNION ALL SELECT runtime_role AS runtime_role FROM BAT_DEPLOYMENT_CELL) role_values WHERE runtime_role IS NOT NULL AND runtime_role NOT IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT', 'CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT')) + (SELECT COUNT(*) FROM user_constraints WHERE constraint_type = 'C' AND ((table_name = UPPER('OPS_RUNTIME_INSTANCE_STATE') AND constraint_name = UPPER('ck_ops_runtime_instance_role')) OR (table_name = UPPER('BAT_RUNTIME_INSTANCE') AND constraint_name = UPPER('ck_bat_runtime_instance_role')) OR (table_name = UPPER('BAT_DEPLOYMENT_CELL') AND constraint_name = UPPER('ck_bat_deployment_runtime_role')))) INTO v_invalid_count FROM dual;
  IF v_invalid_count <> 0 THEN
    raise_application_error(-20009, 'D-009 runtime role preflight failed');
  END IF;
EXCEPTION WHEN OTHERS THEN
  ROLLBACK;
  RAISE;
END;
/
-- D-009 forward: phase 2 applies the data operation and constraint lifecycle.
BEGIN
  UPDATE OPS_RUNTIME_INSTANCE_STATE SET runtime_role = CASE runtime_role WHEN 'CONTROL_SERVER' THEN 'CONTROL_PLANE' WHEN 'CENTER_CUT_RUNNER' THEN 'CENTER_CUT' WHEN 'HOST_AGENT' THEN 'AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT');
  UPDATE BAT_RUNTIME_INSTANCE SET runtime_role = CASE runtime_role WHEN 'CONTROL_SERVER' THEN 'CONTROL_PLANE' WHEN 'CENTER_CUT_RUNNER' THEN 'CENTER_CUT' WHEN 'HOST_AGENT' THEN 'AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT');
  UPDATE BAT_DEPLOYMENT_CELL SET runtime_role = CASE runtime_role WHEN 'CONTROL_SERVER' THEN 'CONTROL_PLANE' WHEN 'CENTER_CUT_RUNNER' THEN 'CENTER_CUT' WHEN 'HOST_AGENT' THEN 'AGENT' ELSE runtime_role END WHERE runtime_role IN ('CONTROL_SERVER', 'CENTER_CUT_RUNNER', 'HOST_AGENT');
EXCEPTION WHEN OTHERS THEN
  ROLLBACK;
  RAISE;
END;
/
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE ADD CONSTRAINT ck_ops_runtime_instance_role CHECK (runtime_role IS NULL OR runtime_role IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT'));
ALTER TABLE BAT_RUNTIME_INSTANCE ADD CONSTRAINT ck_bat_runtime_instance_role CHECK (runtime_role IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT'));
ALTER TABLE BAT_DEPLOYMENT_CELL ADD CONSTRAINT ck_bat_deployment_runtime_role CHECK (runtime_role IN ('CONTROL_PLANE', 'SCHEDULER', 'WORKER', 'CENTER_CUT', 'AGENT'));
COMMIT;
