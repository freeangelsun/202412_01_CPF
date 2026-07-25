-- R13 QA/Product Quality hardening: CSV audit policy, MBR optimistic locking, distributed member number, role idempotency.
-- Existing columns use IF NOT EXISTS because CPF is pre-GA and some development DBs may already contain forward-compatible columns.

USE admDB;

ALTER TABLE adm_download_audit_log
    ADD COLUMN IF NOT EXISTS CSV_POLICY_VERSION VARCHAR(30) NOT NULL DEFAULT 'CPF-CSV-1'
        COMMENT 'CSV spreadsheet injection protection policy version' AFTER USER_AGENT;

USE mbrDB;

ALTER TABLE mbr_member
    ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0
        COMMENT '회원 낙관적 잠금 Version' AFTER description;

ALTER TABLE mbr_member_role
    ADD COLUMN IF NOT EXISTS role_type VARCHAR(30) NOT NULL DEFAULT 'SERVICE'
        COMMENT '회원 역할 유형' AFTER role_name,
    ADD COLUMN IF NOT EXISTS grant_reason VARCHAR(500) NULL
        COMMENT '권한 부여/갱신 사유' AFTER use_yn,
    ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0
        COMMENT '회원 권한 낙관적 잠금 Version' AFTER grant_reason;

CREATE TABLE IF NOT EXISTS mbr_member_no_sequence (
    sequence_value BIGINT NOT NULL AUTO_INCREMENT COMMENT '분산 안전 회원번호 채번 값',
    requested_by VARCHAR(100) NOT NULL COMMENT '채번 요청자',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '채번 시각',
    PRIMARY KEY (sequence_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원번호 분산 채번';

CREATE TABLE IF NOT EXISTS mbr_member_no_issue_history (
    issue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원번호 발급 이력 순번',
    member_no VARCHAR(50) NOT NULL COMMENT '발급 회원번호',
    issue_type VARCHAR(20) NOT NULL COMMENT 'AUTO/MANUAL 발급 유형',
    issued_by VARCHAR(100) NOT NULL COMMENT '발급자',
    issued_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발급 시각',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_mbr_member_no_issue_history_no (member_no),
    INDEX ix_mbr_member_no_issue_history_time (issued_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원번호 발급 이력';

CREATE TABLE IF NOT EXISTS mbr_member_role_operation (
    idempotency_key VARCHAR(120) NOT NULL COMMENT '권한 변경 멱등 키',
    member_id BIGINT NOT NULL COMMENT '회원 순번',
    service_code VARCHAR(30) NOT NULL COMMENT '서비스 코드',
    role_code VARCHAR(50) NOT NULL COMMENT '회원 역할 코드',
    operation_type VARCHAR(20) NOT NULL COMMENT 'GRANT/REVOKE',
    operation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCEEDED',
    result_version BIGINT NULL COMMENT '최초 성공 결과 Version',
    result_use_yn CHAR(1) NULL COMMENT '최초 성공 결과 사용 여부',
    created_by VARCHAR(100) NOT NULL COMMENT '요청자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '요청 시각',
    completed_at DATETIME(3) NULL COMMENT '최초 성공 완료 시각',
    PRIMARY KEY (idempotency_key),
    INDEX ix_mbr_member_role_operation_member (member_id, created_at),
    CONSTRAINT fk_mbr_member_role_operation_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 권한 멱등 변경 이력';
