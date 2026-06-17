-- 업무/교육 샘플 스키마입니다.
-- ACC 계정 샘플과 MBR 회원 샘플은 각 업무 DB에만 배치합니다.

USE accDB;

CREATE TABLE IF NOT EXISTS acc_account (
    account_id INT NOT NULL AUTO_INCREMENT COMMENT '계정 순번',
    account_no VARCHAR(30) NOT NULL COMMENT '계정 번호',
    account_name VARCHAR(100) NOT NULL COMMENT '계정명',
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
    balance DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '잔액',
    description TEXT NULL COMMENT '계정 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (account_id),
    UNIQUE KEY uk_acc_account_no (account_no),
    INDEX ix_acc_account_status (account_status),
    INDEX ix_acc_account_name (account_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACC 계정 샘플';

USE mbrDB;

CREATE TABLE IF NOT EXISTS mbr_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 순번',
    member_no VARCHAR(50) NOT NULL COMMENT '회원 번호',
    customer_no VARCHAR(50) NOT NULL COMMENT '고객 번호',
    login_id VARCHAR(80) NOT NULL COMMENT '로그인 ID',
    name VARCHAR(100) NOT NULL COMMENT '회원명',
    email VARCHAR(200) NULL COMMENT '이메일',
    mobile_no VARCHAR(50) NULL COMMENT '휴대폰 번호',
    member_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태',
    lock_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부',
    withdraw_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '탈퇴 여부',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'WEB' COMMENT '가입 채널 코드',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    last_login_at DATETIME NULL COMMENT '최근 로그인일시',
    description TEXT NULL COMMENT '회원 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mbr_member_no (member_no),
    UNIQUE KEY uk_mbr_member_login_id (login_id),
    INDEX ix_mbr_member_customer (customer_no),
    INDEX ix_mbr_member_name (name),
    INDEX ix_mbr_member_status (member_status, lock_yn, withdraw_yn),
    INDEX ix_mbr_member_channel_joined (channel_code, joined_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원';

CREATE TABLE IF NOT EXISTS mbr_member_role (
    member_role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 권한 순번',
    member_id BIGINT NOT NULL COMMENT '회원 순번',
    service_code VARCHAR(30) NOT NULL DEFAULT 'MBR' COMMENT '서비스 코드',
    role_code VARCHAR(50) NOT NULL COMMENT '회원 역할 코드',
    role_name VARCHAR(100) NULL COMMENT '회원 역할명',
    grade_code VARCHAR(50) NULL COMMENT '회원 등급 코드',
    temporary_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '임시 권한 여부',
    expire_at DATETIME NULL COMMENT '권한 만료일시',
    granted_by VARCHAR(100) NULL COMMENT '권한 부여자',
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '권한 부여일시',
    revoked_by VARCHAR(100) NULL COMMENT '권한 회수자',
    revoked_at DATETIME NULL COMMENT '권한 회수일시',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (member_role_id),
    UNIQUE KEY uk_mbr_member_role (member_id, service_code, role_code),
    INDEX ix_mbr_member_role_member (member_id, use_yn),
    INDEX ix_mbr_member_role_expire (expire_at),
    CONSTRAINT fk_mbr_member_role_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 권한';

CREATE TABLE IF NOT EXISTS mbr_member_role_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 권한 이력 순번',
    member_id BIGINT NOT NULL COMMENT '회원 순번',
    service_code VARCHAR(30) NOT NULL COMMENT '서비스 코드',
    role_code VARCHAR(50) NOT NULL COMMENT '회원 역할 코드',
    action_type VARCHAR(30) NOT NULL COMMENT '권한 행위 유형',
    reason VARCHAR(500) NOT NULL COMMENT '권한 변경 사유',
    before_data LONGTEXT NULL COMMENT '변경 전 데이터',
    after_data LONGTEXT NULL COMMENT '변경 후 데이터',
    operator_id VARCHAR(100) NULL COMMENT '처리 운영자 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (history_id),
    INDEX ix_mbr_member_role_history_member (member_id, created_at),
    INDEX ix_mbr_member_role_history_role (service_code, role_code, created_at),
    CONSTRAINT fk_mbr_member_role_history_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 권한 변경 이력';

CREATE TABLE IF NOT EXISTS mbr_member_login_history (
    login_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 로그인 이력 순번',
    member_id BIGINT NOT NULL COMMENT '회원 순번',
    login_id VARCHAR(80) NOT NULL COMMENT '로그인 ID',
    login_result VARCHAR(30) NOT NULL COMMENT '로그인 결과',
    login_ip VARCHAR(50) NULL COMMENT '로그인 IP',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    failure_reason VARCHAR(500) NULL COMMENT '로그인 실패 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (login_history_id),
    INDEX ix_mbr_member_login_member_time (member_id, created_at),
    INDEX ix_mbr_member_login_result_time (login_result, created_at),
    CONSTRAINT fk_mbr_member_login_history_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 로그인 이력';
