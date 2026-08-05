CREATE TABLE IF NOT EXISTS cmn_template_definition (
    template_code VARCHAR(100) NOT NULL,
    template_version BIGINT NOT NULL,
    channel_code VARCHAR(30) NOT NULL,
    template_body TEXT NOT NULL,
    allowed_variables VARCHAR(2000) NOT NULL DEFAULT '-',
    status_code VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    active_yn CHAR(1) NOT NULL DEFAULT 'N',
    revision_no BIGINT NOT NULL DEFAULT 0,
    approved_by VARCHAR(100),
    approved_at TIMESTAMP(3),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cmn_template_definition PRIMARY KEY (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_version CHECK (template_version > 0),
    CONSTRAINT ck_cmn_template_status CHECK (status_code IN ('DRAFT','APPROVED','RETIRED')),
    CONSTRAINT ck_cmn_template_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_template_revision CHECK (revision_no >= 0)
);
CREATE INDEX ix_cmn_template_active ON cmn_template_definition
    (template_code, channel_code, status_code, active_yn, template_version);
COMMENT ON TABLE cmn_template_definition IS 'CMN Template 제품 정본';

CREATE TABLE IF NOT EXISTS cmn_template_audit (
    audit_id VARCHAR(64) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    template_version BIGINT NOT NULL,
    channel_code VARCHAR(30) NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    request_user VARCHAR(100) NOT NULL,
    request_reason VARCHAR(500) NOT NULL,
    before_status VARCHAR(30),
    after_status VARCHAR(30) NOT NULL,
    revision_no BIGINT NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cmn_template_audit PRIMARY KEY (audit_id),
    CONSTRAINT fk_cmn_template_audit_definition FOREIGN KEY (template_code, template_version, channel_code)
        REFERENCES cmn_template_definition (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_audit_action CHECK (action_type IN ('CREATE_DRAFT','APPROVE','SUPERSEDE','RETIRE')),
    CONSTRAINT ck_cmn_template_audit_revision CHECK (revision_no >= 0)
);
CREATE INDEX ix_cmn_template_audit_lookup ON cmn_template_audit
    (template_code, channel_code, template_version, occurred_at);
COMMENT ON TABLE cmn_template_audit IS 'CMN Template append-only 감사 원장';
