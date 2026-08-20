-- R14 MBW governance/operability hardening.
USE backofficeDB;

ALTER TABLE mbw_menu ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_role ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_permission ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_organization ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_position ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_job_title ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_employee ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;
ALTER TABLE mbw_employee_assignment ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER effective_to;
ALTER TABLE mbw_organization_responsibility ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER use_yn;

-- 기존 (admin_user_id, role_code) PK는 재부여 이력을 막으므로 surrogate PK 기반 이력 모델로 전환한다.
ALTER TABLE mbw_user_role
    DROP PRIMARY KEY,
    ADD COLUMN user_role_id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    ADD COLUMN grant_reason VARCHAR(500) NOT NULL DEFAULT 'MIGRATED' AFTER primary_yn,
    ADD COLUMN operation_id VARCHAR(100) NULL AFTER grant_reason,
    ADD COLUMN version_no BIGINT NOT NULL DEFAULT 0 AFTER operation_id,
    ADD PRIMARY KEY (user_role_id),
    ADD UNIQUE KEY uk_mbw_user_role_operation (operation_id),
    ADD INDEX ix_mbw_user_role_user (admin_user_id, valid_to, primary_yn, user_role_id);

-- 환경/업무/API 범위가 다른 권한 variant를 동일 권한으로 오판하던 과도한 UNIQUE를 제거한다.
DROP INDEX IF EXISTS uk_mbw_permission ON mbw_permission;
CREATE INDEX IF NOT EXISTS ix_mbw_permission_scope
    ON mbw_permission(role_code, menu_code, button_code, environment_code, domain_code, http_method);

CREATE TABLE IF NOT EXISTS mbw_audit_chain_lock (
    chain_id BIGINT NOT NULL COMMENT '감사 체인 식별자. 기본 체인은 1',
    current_hash CHAR(64) NULL COMMENT '현재 감사 체인 head SHA-256',
    last_audit_id BIGINT NULL COMMENT '현재 체인의 마지막 감사 ID',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '체인 갱신 버전',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '마지막 갱신자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '마지막 갱신시각',
    PRIMARY KEY (chain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBW 감사 체인 동시성/무결성 head';

INSERT INTO mbw_audit_chain_lock(chain_id,current_hash,last_audit_id,version_no,updated_by)
VALUES(1,NULL,NULL,0,'R14_MIGRATION')
ON DUPLICATE KEY UPDATE chain_id=VALUES(chain_id);
