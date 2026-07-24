-- CPF 인증 도메인, refresh token hash 저장소, EXS token 이벤트 이력 보강입니다.
-- 신규 설치 기준은 specs/sql/40_business_modules_schema.sql과 00_all_install.sql에 같은 구조가 반영됩니다.

USE mbrDB;

ALTER TABLE mbr_member
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(300) NULL COMMENT '회원 비밀번호 hash' AFTER login_id,
    ADD COLUMN IF NOT EXISTS login_fail_count INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수' AFTER password_hash,
    ADD COLUMN IF NOT EXISTS password_change_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '비밀번호 강제 변경 여부' AFTER login_fail_count,
    ADD COLUMN IF NOT EXISTS password_expire_at DATETIME NULL COMMENT '비밀번호 만료 일시' AFTER password_change_required_yn;

ALTER TABLE mbr_member_login_history
    ADD COLUMN IF NOT EXISTS login_domain VARCHAR(30) NOT NULL DEFAULT 'MBR' COMMENT '로그인 도메인' AFTER member_id,
    ADD COLUMN IF NOT EXISTS member_no VARCHAR(50) NULL COMMENT '회원 번호' AFTER login_domain,
    ADD COLUMN IF NOT EXISTS customer_no VARCHAR(50) NULL COMMENT '고객 번호' AFTER member_no,
    ADD COLUMN IF NOT EXISTS transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 트랜잭션 글로벌 ID' AFTER failure_reason,
    ADD COLUMN IF NOT EXISTS module_id VARCHAR(3) NULL COMMENT '모듈 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS was_id VARCHAR(7) NULL COMMENT 'WAS ID' AFTER module_id,
    ADD COLUMN IF NOT EXISTS server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID' AFTER was_id,
    ADD INDEX IF NOT EXISTS ix_mbr_member_login_global (transaction_global_id);

CREATE TABLE IF NOT EXISTS mbr_refresh_token (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 refresh token 순번',
    member_id BIGINT NOT NULL COMMENT '회원 순번',
    member_no VARCHAR(50) NOT NULL COMMENT '회원 번호',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'MBR' COMMENT '로그인 도메인',
    refresh_token_hash VARCHAR(300) NOT NULL COMMENT 'refresh token hash',
    transaction_global_id VARCHAR(34) NULL COMMENT '발급 트랜잭션 글로벌 ID',
    expire_at DATETIME NOT NULL COMMENT '만료 일시',
    revoked_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '폐기 여부',
    revoked_at DATETIME NULL COMMENT '폐기 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY uk_mbr_refresh_token_hash (refresh_token_hash),
    INDEX ix_mbr_refresh_token_member (member_id, revoked_yn, expire_at),
    CONSTRAINT fk_mbr_refresh_token_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 refresh token hash 저장소';

USE bizadmDB;

ALTER TABLE bizadm_admin_user
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(300) NULL COMMENT '업무 관리자 비밀번호 hash' AFTER admin_name,
    ADD COLUMN IF NOT EXISTS lock_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부' AFTER use_yn,
    ADD COLUMN IF NOT EXISTS login_fail_count INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수' AFTER lock_yn,
    ADD COLUMN IF NOT EXISTS password_change_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '비밀번호 강제 변경 여부' AFTER login_fail_count,
    ADD COLUMN IF NOT EXISTS password_expire_at DATETIME NULL COMMENT '비밀번호 만료 일시' AFTER password_change_required_yn,
    ADD COLUMN IF NOT EXISTS last_login_at DATETIME NULL COMMENT '최근 로그인 일시' AFTER password_expire_at;

CREATE TABLE IF NOT EXISTS bizadm_login_history (
    login_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 로그인 이력 순번',
    admin_user_id BIGINT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'BIZADM' COMMENT '로그인 도메인',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    login_result VARCHAR(30) NOT NULL COMMENT '로그인 결과',
    failure_reason VARCHAR(500) NULL COMMENT '로그인 실패 사유',
    client_ip VARCHAR(50) NULL COMMENT '클라이언트 IP',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    module_id VARCHAR(3) NULL COMMENT '모듈 ID',
    was_id VARCHAR(7) NULL COMMENT 'WAS ID',
    server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (login_history_id),
    INDEX ix_bizadm_login_history_user_time (admin_user_id, created_at),
    INDEX ix_bizadm_login_history_result_time (login_result, created_at),
    INDEX ix_bizadm_login_history_global (transaction_global_id),
    CONSTRAINT fk_bizadm_login_history_user
        FOREIGN KEY (admin_user_id) REFERENCES bizadm_admin_user(admin_user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 관리자 로그인 이력';

CREATE TABLE IF NOT EXISTS bizadm_refresh_token (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 refresh token 순번',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'BIZADM' COMMENT '로그인 도메인',
    refresh_token_hash VARCHAR(300) NOT NULL COMMENT 'refresh token hash',
    transaction_global_id VARCHAR(34) NULL COMMENT '발급 트랜잭션 글로벌 ID',
    expire_at DATETIME NOT NULL COMMENT '만료 일시',
    revoked_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '폐기 여부',
    revoked_at DATETIME NULL COMMENT '폐기 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY uk_bizadm_refresh_token_hash (refresh_token_hash),
    INDEX ix_bizadm_refresh_token_user (admin_user_id, revoked_yn, expire_at),
    CONSTRAINT fk_bizadm_refresh_token_user
        FOREIGN KEY (admin_user_id) REFERENCES bizadm_admin_user(admin_user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 관리자 refresh token hash 저장소';

USE exsDB;

ALTER TABLE exs_token_store
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(300) NULL COMMENT '대외 token hash' AFTER token_key,
    ADD COLUMN IF NOT EXISTS masked_token VARCHAR(200) NULL COMMENT '마스킹 token 표시값' AFTER token_hash,
    ADD COLUMN IF NOT EXISTS issued_at DATETIME NULL COMMENT '발급 일시' AFTER token_status,
    ADD COLUMN IF NOT EXISTS transaction_global_id VARCHAR(34) NULL COMMENT '발급 트랜잭션 글로벌 ID' AFTER expire_at,
    ADD COLUMN IF NOT EXISTS server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID' AFTER transaction_global_id,
    ADD INDEX IF NOT EXISTS ix_exs_token_store_hash (token_hash);

CREATE TABLE IF NOT EXISTS exs_token_event_history (
    token_event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 token 이벤트 순번',
    auth_profile_code VARCHAR(80) NOT NULL COMMENT '대외 인증 프로파일 코드',
    token_key VARCHAR(120) NOT NULL COMMENT '토큰 식별 키',
    event_type VARCHAR(50) NOT NULL COMMENT 'token 이벤트 유형',
    reason VARCHAR(500) NULL COMMENT '이벤트 사유',
    transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (token_event_id),
    INDEX ix_exs_token_event_profile_time (auth_profile_code, created_at),
    INDEX ix_exs_token_event_global (transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 token 이벤트 이력';
