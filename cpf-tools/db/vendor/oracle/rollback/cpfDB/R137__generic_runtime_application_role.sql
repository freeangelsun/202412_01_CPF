DECLARE
  v_application_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_application_count
  FROM OPS_RUNTIME_INSTANCE_STATE
  WHERE runtime_role = 'APPLICATION';
  IF v_application_count <> 0 THEN
    RAISE_APPLICATION_ERROR(-20137, 'R137 blocked: APPLICATION Runtime registrations still exist');
  END IF;
END;
/
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE
    ADD CONSTRAINT ck_ops_runtime_instance_role
    CHECK (runtime_role IS NULL OR runtime_role IN ('CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT','AGENT'));
