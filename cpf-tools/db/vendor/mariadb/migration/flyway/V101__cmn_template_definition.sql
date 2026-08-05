CREATE TABLE IF NOT EXISTS cmn_template_definition (
    template_code VARCHAR(100) NOT NULL COMMENT 'Template 식별 코드',
    template_version BIGINT NOT NULL COMMENT '불변 Template 버전',
    channel_code VARCHAR(30) NOT NULL COMMENT '채널 코드',
    template_body LONGTEXT NOT NULL COMMENT 'Template 본문',
    allowed_variables VARCHAR(2000) NOT NULL DEFAULT '-' COMMENT '허용 변수 Schema',
    status_code VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '상태',
    active_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '활성 여부',
    revision_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 Revision',
    approved_by VARCHAR(100) NULL COMMENT '승인자',
    approved_at DATETIME(3) NULL COMMENT '승인 일시',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정 일시',
    CONSTRAINT pk_cmn_template_definition PRIMARY KEY (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_version CHECK (template_version > 0),
    CONSTRAINT ck_cmn_template_status CHECK (status_code IN ('DRAFT','APPROVED','RETIRED')),
    CONSTRAINT ck_cmn_template_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_template_revision CHECK (revision_no >= 0),
    INDEX ix_cmn_template_active (template_code, channel_code, status_code, active_yn, template_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN Template 제품 정본';

CREATE TABLE IF NOT EXISTS cmn_template_audit (
    audit_id VARCHAR(64) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    template_version BIGINT NOT NULL,
    channel_code VARCHAR(30) NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    request_user VARCHAR(100) NOT NULL,
    request_reason VARCHAR(500) NOT NULL,
    before_status VARCHAR(30) NULL,
    after_status VARCHAR(30) NOT NULL,
    revision_no BIGINT NOT NULL,
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_cmn_template_audit PRIMARY KEY (audit_id),
    CONSTRAINT fk_cmn_template_audit_definition FOREIGN KEY (template_code, template_version, channel_code)
        REFERENCES cmn_template_definition (template_code, template_version, channel_code),
    CONSTRAINT ck_cmn_template_audit_action CHECK (action_type IN ('CREATE_DRAFT','APPROVE','SUPERSEDE','RETIRE')),
    CONSTRAINT ck_cmn_template_audit_revision CHECK (revision_no >= 0),
    INDEX ix_cmn_template_audit_lookup (template_code, channel_code, template_version, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN Template append-only 감사 원장';
