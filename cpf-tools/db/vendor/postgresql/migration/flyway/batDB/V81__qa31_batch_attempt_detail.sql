-- QA31-D019: durable shell/file execution attempt details.
ALTER TABLE bat_execution_attempt
    ADD COLUMN executor_type VARCHAR(40),
    ADD COLUMN exit_code INTEGER,
    ADD COLUMN stdout_text TEXT,
    ADD COLUMN stderr_text TEXT,
    ADD COLUMN output_truncated_yn CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN duration_ms BIGINT,
    ADD COLUMN artifact_hash VARCHAR(128),
    ADD COLUMN unknown_result_yn CHAR(1) NOT NULL DEFAULT 'N',
    ADD CONSTRAINT ck_bat_execution_attempt_truncated CHECK (output_truncated_yn IN ('Y','N')),
    ADD CONSTRAINT ck_bat_execution_attempt_unknown CHECK (unknown_result_yn IN ('Y','N'));
