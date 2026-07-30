-- QA31-D023: durable multi-instance ADM log export artifact.
CREATE TABLE adm_log_export_artifact (
    export_id VARCHAR(64) NOT NULL,
    owner_operator_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    artifact_content BYTEA NOT NULL,
    content_length BIGINT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    status_code VARCHAR(20) NOT NULL,
    downloaded_at TIMESTAMP(3),
    download_count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_adm_log_export_artifact PRIMARY KEY (export_id)
);
CREATE INDEX ix_adm_log_export_expiry ON adm_log_export_artifact (expires_at);
CREATE INDEX ix_adm_log_export_owner ON adm_log_export_artifact (owner_operator_id, created_at);
