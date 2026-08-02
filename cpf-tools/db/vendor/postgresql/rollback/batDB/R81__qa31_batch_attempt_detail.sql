-- Data-destructive rollback. Export attempt details before execution.
ALTER TABLE bat_execution_attempt
    DROP CONSTRAINT IF EXISTS ck_bat_execution_attempt_unknown,
    DROP CONSTRAINT IF EXISTS ck_bat_execution_attempt_truncated,
    DROP COLUMN IF EXISTS unknown_result_yn,
    DROP COLUMN IF EXISTS artifact_hash,
    DROP COLUMN IF EXISTS duration_ms,
    DROP COLUMN IF EXISTS output_truncated_yn,
    DROP COLUMN IF EXISTS stderr_text,
    DROP COLUMN IF EXISTS stdout_text,
    DROP COLUMN IF EXISTS exit_code,
    DROP COLUMN IF EXISTS executor_type;
