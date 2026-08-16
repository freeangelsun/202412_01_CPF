CREATE TABLE cmn_template_definition (
    template_code VARCHAR2(100 CHAR) NOT NULL,
    template_version NUMBER(19) NOT NULL,
    channel_code VARCHAR2(30 CHAR) NOT NULL,
    template_body CLOB NOT NULL,
    allowed_variables VARCHAR2(2000 CHAR) DEFAULT '-' NOT NULL,
    status_code VARCHAR2(20 CHAR) DEFAULT 'DRAFT' NOT NULL,
    active_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    revision_no NUMBER(19) DEFAULT 0 NOT NULL,
    approved_by VARCHAR2(100 CHAR),
    approved_at TIMESTAMP(3),
    created_by VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100 CHAR) NOT NULL,
    updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cmn_template_definition PRIMARY KEY (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_version CHECK (template_version > 0),
    CONSTRAINT ck_cmn_template_status CHECK (status_code IN ('DRAFT','APPROVED','RETIRED')),
    CONSTRAINT ck_cmn_template_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_template_revision CHECK (revision_no >= 0)
);
CREATE INDEX ix_cmn_template_active ON cmn_template_definition
    (template_code, channel_code, status_code, active_yn, template_version);
COMMENT ON TABLE cmn_template_definition IS 'CMN Template 제품 정본';

CREATE TABLE cmn_template_audit (
    audit_id VARCHAR2(64 CHAR) NOT NULL,
    template_code VARCHAR2(100 CHAR) NOT NULL,
    template_version NUMBER(19) NOT NULL,
    channel_code VARCHAR2(30 CHAR) NOT NULL,
    action_type VARCHAR2(30 CHAR) NOT NULL,
    request_user VARCHAR2(100 CHAR) NOT NULL,
    request_reason VARCHAR2(500 CHAR) NOT NULL,
    before_status VARCHAR2(30 CHAR),
    after_status VARCHAR2(30 CHAR) NOT NULL,
    revision_no NUMBER(19) NOT NULL,
    occurred_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cmn_template_audit PRIMARY KEY (audit_id),
    CONSTRAINT fk_cmn_template_audit_definition FOREIGN KEY (template_code, template_version, channel_code)
        REFERENCES cmn_template_definition (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_audit_action CHECK (action_type IN ('CREATE_DRAFT','APPROVE','SUPERSEDE','RETIRE')),
    CONSTRAINT ck_cmn_template_audit_revision CHECK (revision_no >= 0)
);
CREATE INDEX ix_cmn_template_audit_lookup ON cmn_template_audit
    (template_code, channel_code, template_version, occurred_at);
COMMENT ON TABLE cmn_template_audit IS 'CMN Template append-only 감사 원장';
