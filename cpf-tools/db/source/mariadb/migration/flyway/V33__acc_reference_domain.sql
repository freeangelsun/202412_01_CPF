-- 생성기 검증용 ACC reference domain의 계정과 감사 이력 테이블을 설치합니다.
CREATE DATABASE IF NOT EXISTS accDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS accDB.acc_account (
    account_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '계정 식별자',
    account_no VARCHAR(50) NOT NULL COMMENT '업무 계정번호',
    account_name VARCHAR(150) NOT NULL COMMENT '계정명',
    email VARCHAR(200) NULL COMMENT '마스킹 대상 이메일',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태 코드',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (account_id),
    UNIQUE KEY uk_acc_account_no (account_no),
    INDEX ix_acc_account_search (status_code, deleted_yn, account_id),
    CONSTRAINT ck_acc_account_deleted CHECK (deleted_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACC 중립 계정 reference';

CREATE TABLE IF NOT EXISTS accDB.acc_account_change_log (
    account_change_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '계정 변경 로그 순번',
    account_id BIGINT NOT NULL COMMENT '변경 계정 식별자',
    action_code VARCHAR(30) NOT NULL COMMENT 'CREATE, UPDATE 또는 DELETE 행위 코드',
    before_value LONGTEXT NULL COMMENT '마스킹된 변경 전 값',
    after_value LONGTEXT NULL COMMENT '마스킹된 변경 후 값',
    audit_reason VARCHAR(500) NOT NULL COMMENT '변경 감사 사유',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (account_change_log_id),
    INDEX ix_acc_account_change_target (account_id, created_at),
    CONSTRAINT fk_acc_account_change_target FOREIGN KEY (account_id)
        REFERENCES accDB.acc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACC 계정 변경 감사 이력';
