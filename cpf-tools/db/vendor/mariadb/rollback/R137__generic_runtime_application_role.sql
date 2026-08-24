-- CPF_LOGICAL_DATABASE=cpfDB
-- Rollback is fail-closed while any generic APPLICATION runtime remains registered.
CREATE TEMPORARY TABLE cpf_v137_runtime_role_guard (
    invalid_count BIGINT NOT NULL,
    CONSTRAINT ck_cpf_v137_runtime_role_guard CHECK (invalid_count = 0)
);
INSERT INTO cpf_v137_runtime_role_guard(invalid_count)
SELECT COUNT(*) FROM OPS_RUNTIME_INSTANCE_STATE WHERE BINARY runtime_role = 'APPLICATION';
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE DROP CONSTRAINT ck_ops_runtime_instance_role;
ALTER TABLE OPS_RUNTIME_INSTANCE_STATE
    ADD CONSTRAINT ck_ops_runtime_instance_role
    CHECK (runtime_role IS NULL OR BINARY runtime_role IN ('CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT','AGENT'));
DROP TEMPORARY TABLE cpf_v137_runtime_role_guard;
