-- QA31-D023: durable multi-instance ADM log export artifact.
CREATE TABLE adm_log_export_artifact (
    export_id VARCHAR2(64 CHAR) NOT NULL,
    owner_operator_id VARCHAR2(100 CHAR) NOT NULL,
    file_name VARCHAR2(255 CHAR) NOT NULL,
    content_type VARCHAR2(100 CHAR) NOT NULL,
    artifact_content BLOB NOT NULL,
    content_length NUMBER(19) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    status_code VARCHAR2(20 CHAR) NOT NULL,
    downloaded_at TIMESTAMP(3),
    download_count NUMBER(19) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_adm_log_export_artifact PRIMARY KEY (export_id)
);
CREATE INDEX ix_adm_log_export_expiry ON adm_log_export_artifact (expires_at);
CREATE INDEX ix_adm_log_export_owner ON adm_log_export_artifact (owner_operator_id, created_at);
