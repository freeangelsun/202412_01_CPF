DROP TABLE IF EXISTS adm_log_export_artifact;
DROP TABLE IF EXISTS cpf_gateway_control_security_audit;
-- Data-destructive rollback. Execute only after exporting QA31 attempt detail.
DROP TABLE cpf_gateway_control_nonce;
ALTER TABLE bat_execution_attempt
    DROP CONSTRAINT ck_bat_execution_attempt_unknown,
    DROP CONSTRAINT ck_bat_execution_attempt_truncated,
    DROP COLUMN unknown_result_yn,
    DROP COLUMN artifact_hash,
    DROP COLUMN duration_ms,
    DROP COLUMN output_truncated_yn,
    DROP COLUMN stderr_text,
    DROP COLUMN stdout_text,
    DROP COLUMN exit_code,
    DROP COLUMN executor_type;
ALTER TABLE cpf_gateway_binding DROP COLUMN target_path;
