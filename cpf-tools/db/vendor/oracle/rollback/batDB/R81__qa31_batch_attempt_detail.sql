-- Data-destructive rollback. Export attempt details before execution.
ALTER TABLE bat_execution_attempt DROP CONSTRAINT ck_bat_execution_attempt_unknown;
ALTER TABLE bat_execution_attempt DROP CONSTRAINT ck_bat_execution_attempt_truncated;
ALTER TABLE bat_execution_attempt DROP (
    unknown_result_yn, artifact_hash, duration_ms, output_truncated_yn,
    stderr_text, stdout_text, exit_code, executor_type
);
