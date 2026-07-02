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

USE xyzDB;

CREATE TABLE IF NOT EXISTS xyz_center_cut_sample_target (
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NOT NULL COMMENT '업무 기준일',
    target_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    status_code VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태 코드',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    started_at DATETIME NULL COMMENT '처리 시작 일시',
    completed_at DATETIME NULL COMMENT '처리 완료 일시',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'XYZ' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'XYZ' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    UNIQUE KEY uk_xyz_center_cut_sample_target_business (center_cut_job_id, business_key),
    INDEX ix_xyz_center_cut_sample_target_status (center_cut_job_id, status_code, business_date),
    INDEX ix_xyz_center_cut_sample_target_global (parent_transaction_global_id, child_transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='XYZ 센터컷 샘플 대상';

CREATE TABLE IF NOT EXISTS xyz_center_cut_sample_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 샘플 결과 순번',
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'XYZ' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'XYZ' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_xyz_center_cut_sample_result_target (target_id),
    INDEX ix_xyz_center_cut_sample_result_job (center_cut_job_id, result_status, created_at),
    INDEX ix_xyz_center_cut_sample_result_global (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_xyz_center_cut_sample_result_target
        FOREIGN KEY (target_id) REFERENCES xyz_center_cut_sample_target(target_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='XYZ 센터컷 샘플 결과';

USE mbrDB;

CREATE TABLE IF NOT EXISTS mbr_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 순번',
    member_no VARCHAR(50) NOT NULL COMMENT '회원 번호',
    customer_no VARCHAR(50) NOT NULL COMMENT '고객 번호',
    login_id VARCHAR(80) NOT NULL COMMENT '로그인 ID',
    password_hash VARCHAR(300) NULL COMMENT '회원 비밀번호 hash',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    password_change_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '비밀번호 강제 변경 여부',
    password_expire_at DATETIME NULL COMMENT '비밀번호 만료 일시',
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
    member_id BIGINT NULL COMMENT '회원 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'MBR' COMMENT '로그인 도메인',
    member_no VARCHAR(50) NULL COMMENT '회원 번호',
    customer_no VARCHAR(50) NULL COMMENT '고객 번호',
    login_id VARCHAR(80) NOT NULL COMMENT '로그인 ID',
    login_result VARCHAR(30) NOT NULL COMMENT '로그인 결과',
    login_ip VARCHAR(50) NULL COMMENT '로그인 IP',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    failure_reason VARCHAR(500) NULL COMMENT '로그인 실패 사유',
    transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    module_id VARCHAR(3) NULL COMMENT '모듈 ID',
    was_id VARCHAR(7) NULL COMMENT 'WAS ID',
    server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (login_history_id),
    INDEX ix_mbr_member_login_member_time (member_id, created_at),
    INDEX ix_mbr_member_login_result_time (login_result, created_at),
    INDEX ix_mbr_member_login_global (transaction_global_id),
    CONSTRAINT fk_mbr_member_login_history_member
        FOREIGN KEY (member_id) REFERENCES mbr_member(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBR 회원 로그인 이력';

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

CREATE TABLE IF NOT EXISTS bizadm_admin_user (
    admin_user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 사용자 순번',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    admin_name VARCHAR(100) NOT NULL COMMENT '업무 관리자명',
    password_hash VARCHAR(300) NULL COMMENT '업무 관리자 비밀번호 hash',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 관리자 역할 코드',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    lock_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    password_change_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '비밀번호 강제 변경 여부',
    password_expire_at DATETIME NULL COMMENT '비밀번호 만료 일시',
    last_login_at DATETIME NULL COMMENT '최근 로그인 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (admin_user_id),
    UNIQUE KEY uk_bizadm_admin_user_login (admin_login_id),
    INDEX ix_bizadm_admin_user_role (role_code, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 관리자 사용자';

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

CREATE TABLE IF NOT EXISTS bizadm_menu (
    menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 메뉴 순번',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    menu_name VARCHAR(120) NOT NULL COMMENT '업무 메뉴명',
    api_path VARCHAR(300) NULL COMMENT '연결 API 경로',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (menu_id),
    UNIQUE KEY uk_bizadm_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 메뉴';

CREATE TABLE IF NOT EXISTS bizadm_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 역할 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    role_name VARCHAR(120) NOT NULL COMMENT '업무 역할명',
    write_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '쓰기 허용 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_bizadm_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 역할';

CREATE TABLE IF NOT EXISTS bizadm_permission (
    permission_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 권한 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    button_code VARCHAR(80) NOT NULL COMMENT '버튼/행위 코드',
    allow_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_bizadm_permission (role_code, menu_code, button_code),
    INDEX ix_bizadm_permission_menu (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 권한';

CREATE TABLE IF NOT EXISTS bizadm_customer (
    customer_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '고객 샘플 순번',
    customer_no VARCHAR(50) NOT NULL COMMENT '고객 번호',
    customer_name VARCHAR(100) NOT NULL COMMENT '고객명',
    email VARCHAR(200) NULL COMMENT '이메일',
    mobile_no VARCHAR(50) NULL COMMENT '휴대폰 번호',
    customer_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '고객 상태',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (customer_id),
    UNIQUE KEY uk_bizadm_customer_no (customer_no),
    INDEX ix_bizadm_customer_status (customer_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 고객';

CREATE TABLE IF NOT EXISTS bizadm_product (
    product_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 샘플 순번',
    product_code VARCHAR(50) NOT NULL COMMENT '상품 코드',
    product_name VARCHAR(120) NOT NULL COMMENT '상품명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (product_id),
    UNIQUE KEY uk_bizadm_product_code (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 상품';

CREATE TABLE IF NOT EXISTS bizadm_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문 샘플 순번',
    order_no VARCHAR(50) NOT NULL COMMENT '주문 번호',
    customer_no VARCHAR(50) NOT NULL COMMENT '고객 번호',
    product_code VARCHAR(50) NOT NULL COMMENT '상품 코드',
    order_amount DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '주문 금액',
    order_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT '주문 상태',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_bizadm_order_no (order_no),
    INDEX ix_bizadm_order_customer (customer_no),
    INDEX ix_bizadm_order_product (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 주문';

CREATE TABLE IF NOT EXISTS bizadm_project_setting (
    setting_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 설정 순번',
    setting_key VARCHAR(120) NOT NULL COMMENT '업무 설정 키',
    setting_value VARCHAR(1000) NULL COMMENT '업무 설정 값',
    description VARCHAR(500) NULL COMMENT '업무 설정 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (setting_id),
    UNIQUE KEY uk_bizadm_project_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 프로젝트 설정';

CREATE TABLE IF NOT EXISTS bizadm_masking_audit (
    masking_audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '마스킹 감사 샘플 순번',
    target_type VARCHAR(80) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(120) NOT NULL COMMENT '대상 ID',
    operator_id VARCHAR(100) NOT NULL COMMENT '처리 운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '마스킹 해제 사유',
    result_type VARCHAR(20) NOT NULL COMMENT '처리 결과 유형',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (masking_audit_id),
    INDEX ix_bizadm_masking_audit_target (target_type, target_id, created_at),
    INDEX ix_bizadm_masking_audit_operator (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 마스킹 감사';

USE exsDB;

CREATE TABLE IF NOT EXISTS exs_institution (
    institution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외기관 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    institution_name VARCHAR(120) NOT NULL COMMENT '대외기관명',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (institution_id),
    UNIQUE KEY uk_exs_institution_code (institution_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외기관';

CREATE TABLE IF NOT EXISTS exs_channel (
    channel_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 채널 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    channel_code VARCHAR(50) NOT NULL COMMENT '대외 채널 코드',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '채널 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (channel_id),
    UNIQUE KEY uk_exs_channel_code (institution_code, channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 채널';

CREATE TABLE IF NOT EXISTS exs_endpoint (
    endpoint_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 endpoint 순번',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    endpoint_uri VARCHAR(500) NOT NULL COMMENT '대외 endpoint URI',
    timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '호출 timeout 밀리초',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '기본 재시도 횟수',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'endpoint 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (endpoint_id),
    UNIQUE KEY uk_exs_endpoint_code (endpoint_code),
    INDEX ix_exs_endpoint_institution (institution_code, enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 endpoint';

CREATE TABLE IF NOT EXISTS exs_auth_profile (
    auth_profile_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 인증 프로파일 순번',
    auth_profile_code VARCHAR(80) NOT NULL COMMENT '대외 인증 프로파일 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    auth_type VARCHAR(30) NOT NULL COMMENT '인증 유형',
    secret_ref VARCHAR(300) NULL COMMENT 'secret 참조 경로',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '인증 프로파일 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (auth_profile_id),
    UNIQUE KEY uk_exs_auth_profile_code (auth_profile_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 인증 프로파일';

CREATE TABLE IF NOT EXISTS exs_token_store (
    token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 토큰 순번',
    auth_profile_code VARCHAR(80) NOT NULL COMMENT '대외 인증 프로파일 코드',
    token_key VARCHAR(120) NOT NULL COMMENT '토큰 식별 키',
    token_hash VARCHAR(300) NULL COMMENT '대외 token hash',
    masked_token VARCHAR(200) NULL COMMENT '마스킹 token 표시값',
    token_status VARCHAR(30) NOT NULL COMMENT '토큰 상태',
    issued_at DATETIME NULL COMMENT '발급 일시',
    expire_at DATETIME NULL COMMENT '토큰 만료일시',
    transaction_global_id VARCHAR(34) NULL COMMENT '발급 트랜잭션 글로벌 ID',
    server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (token_id),
    UNIQUE KEY uk_exs_token_store_key (auth_profile_code, token_key),
    INDEX ix_exs_token_store_expire (expire_at),
    INDEX ix_exs_token_store_hash (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 토큰 저장소';

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

CREATE TABLE IF NOT EXISTS exs_route_rule (
    route_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 라우팅 규칙 순번',
    route_code VARCHAR(80) NOT NULL COMMENT '대외 라우팅 규칙 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    channel_code VARCHAR(50) NOT NULL COMMENT '대외 채널 코드',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '라우팅 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (route_id),
    UNIQUE KEY uk_exs_route_rule_code (route_code),
    INDEX ix_exs_route_rule_target (institution_code, channel_code, enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 라우팅 규칙';

CREATE TABLE IF NOT EXISTS exs_transaction_log (
    transaction_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 거래 로그 순번',
    transaction_global_id VARCHAR(34) NOT NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    external_transaction_id VARCHAR(120) NULL COMMENT '대외기관 거래 ID',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    channel_code VARCHAR(50) NOT NULL COMMENT '대외 채널 코드',
    endpoint_code VARCHAR(80) NULL COMMENT '대외 endpoint 코드',
    module_id VARCHAR(3) NOT NULL COMMENT '처리 모듈 ID',
    was_id VARCHAR(7) NOT NULL COMMENT '처리 WAS ID',
    server_instance_id VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID',
    request_at DATETIME(3) NOT NULL COMMENT '요청 수신/송신 일시',
    response_at DATETIME(3) NULL COMMENT '응답 수신/송신 일시',
    elapsed_ms BIGINT NULL COMMENT '처리 시간 밀리초',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    http_method VARCHAR(10) NULL COMMENT 'HTTP 메서드',
    request_uri VARCHAR(500) NULL COMMENT '요청 URI',
    status VARCHAR(30) NOT NULL COMMENT '처리 상태',
    result_code VARCHAR(50) NULL COMMENT '처리 결과 코드',
    error_code VARCHAR(100) NULL COMMENT '오류 코드',
    error_message VARCHAR(1000) NULL COMMENT '마스킹된 오류 메시지',
    retryable_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '재처리 가능 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (transaction_log_id),
    INDEX ix_exs_transaction_log_global (transaction_global_id),
    INDEX ix_exs_transaction_log_external (external_transaction_id),
    INDEX ix_exs_transaction_log_target_time (institution_code, channel_code, request_at),
    INDEX ix_exs_transaction_log_status_time (status, request_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 거래 로그';

CREATE TABLE IF NOT EXISTS exs_message_log (
    message_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 송수신 로그 순번',
    transaction_global_id VARCHAR(34) NOT NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    external_transaction_id VARCHAR(120) NULL COMMENT '대외기관 거래 ID',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    message_summary VARCHAR(1000) NULL COMMENT '마스킹된 전문 요약',
    payload_store_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '원문 별도 저장 여부',
    payload_ref VARCHAR(300) NULL COMMENT '원문 저장 참조',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (message_log_id),
    INDEX ix_exs_message_log_global (transaction_global_id, created_at),
    INDEX ix_exs_message_log_external (external_transaction_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 송수신 로그';

CREATE TABLE IF NOT EXISTS exs_control_policy (
    control_policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 통제 정책 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    control_type VARCHAR(30) NOT NULL COMMENT '통제 유형',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    reason VARCHAR(500) NULL COMMENT '통제 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (control_policy_id),
    UNIQUE KEY uk_exs_control_policy (institution_code, control_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 통제 정책';

CREATE TABLE IF NOT EXISTS exs_retry_log (
    retry_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 재처리 로그 순번',
    transaction_global_id VARCHAR(34) NOT NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    external_transaction_id VARCHAR(120) NULL COMMENT '대외기관 거래 ID',
    retry_status VARCHAR(30) NOT NULL COMMENT '재처리 상태',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마스킹된 마지막 오류 메시지',
    next_retry_at DATETIME NULL COMMENT '다음 재처리 예정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (retry_log_id),
    INDEX ix_exs_retry_log_global (transaction_global_id, retry_status),
    INDEX ix_exs_retry_log_next (next_retry_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 재처리 로그';
