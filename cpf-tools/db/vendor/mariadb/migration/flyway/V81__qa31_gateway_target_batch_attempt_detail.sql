-- QA31-D008/D019: ingress/target path separation and durable batch attempt detail.
CREATE TABLE cpf_gateway_control_nonce (
    audience VARCHAR(160) NOT NULL,
    key_id VARCHAR(80) NOT NULL,
    caller_id VARCHAR(80) NOT NULL,
    nonce VARCHAR(160) NOT NULL,
    claimed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expires_at DATETIME(3) NOT NULL,
    PRIMARY KEY (audience, key_id, caller_id, nonce),
    KEY ix_cpf_gateway_control_nonce_expiry (expires_at)
);

ALTER TABLE cpf_gateway_binding
    ADD COLUMN target_path VARCHAR(500) NULL AFTER path_pattern;
UPDATE cpf_gateway_binding
   SET target_path = path_pattern
 WHERE target_path IS NULL;
ALTER TABLE cpf_gateway_binding
    MODIFY COLUMN target_path VARCHAR(500) NOT NULL;

ALTER TABLE bat_execution_attempt
    ADD COLUMN executor_type VARCHAR(40) NULL AFTER result_message,
    ADD COLUMN exit_code INT NULL AFTER executor_type,
    ADD COLUMN stdout_text MEDIUMTEXT NULL AFTER exit_code,
    ADD COLUMN stderr_text MEDIUMTEXT NULL AFTER stdout_text,
    ADD COLUMN output_truncated_yn CHAR(1) NOT NULL DEFAULT 'N' AFTER stderr_text,
    ADD COLUMN duration_ms BIGINT NULL AFTER output_truncated_yn,
    ADD COLUMN artifact_hash VARCHAR(128) NULL AFTER duration_ms,
    ADD COLUMN unknown_result_yn CHAR(1) NOT NULL DEFAULT 'N' AFTER artifact_hash,
    ADD CONSTRAINT ck_bat_execution_attempt_truncated CHECK (output_truncated_yn IN ('Y','N')),
    ADD CONSTRAINT ck_bat_execution_attempt_unknown CHECK (unknown_result_yn IN ('Y','N'));

CREATE TABLE cpf_gateway_control_security_audit (
    event_id VARCHAR(64) NOT NULL,
    occurred_at DATETIME(3) NOT NULL,
    audience VARCHAR(160) NULL,
    key_id VARCHAR(80) NULL,
    caller_service VARCHAR(80) NULL,
    operator_id VARCHAR(100) NULL,
    http_method VARCHAR(16) NULL,
    request_target VARCHAR(1000) NULL,
    remote_address VARCHAR(128) NULL,
    result_code VARCHAR(80) NOT NULL,
    safe_message VARCHAR(1000) NULL,
    PRIMARY KEY (event_id),
    KEY ix_cpf_gw_ctl_sec_audit_time (occurred_at)
);

CREATE TABLE adm_log_export_artifact (
    export_id VARCHAR(64) NOT NULL,
    owner_operator_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    artifact_content LONGBLOB NOT NULL,
    content_length BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    status_code VARCHAR(20) NOT NULL,
    downloaded_at DATETIME(3) NULL,
    download_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (export_id),
    KEY ix_adm_log_export_expiry (expires_at),
    KEY ix_adm_log_export_owner (owner_operator_id, created_at)
);
