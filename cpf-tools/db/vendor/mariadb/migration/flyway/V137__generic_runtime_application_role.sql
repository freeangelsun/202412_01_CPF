-- CPF_LOGICAL_DATABASE=cpfDB
-- Shared OPS Runtime state accepts canonical non-Batch applications; BAT role tables remain unchanged.
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE
    ADD CONSTRAINT ck_ops_runtime_instance_role
    CHECK (runtime_role IS NULL OR BINARY runtime_role IN ('APPLICATION','CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT','AGENT'));
