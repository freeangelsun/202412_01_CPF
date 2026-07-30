-- QA31-D019: durable shell/file execution attempt details.
ALTER TABLE bat_execution_attempt ADD (
    executor_type VARCHAR2(40 CHAR),
    exit_code NUMBER(10),
    stdout_text CLOB,
    stderr_text CLOB,
    output_truncated_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    duration_ms NUMBER(19),
    artifact_hash VARCHAR2(128 CHAR),
    unknown_result_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    CONSTRAINT ck_bat_execution_attempt_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT ck_bat_execution_attempt_unknown CHECK (unknown_result_yn IN ('Y','N'))
);
