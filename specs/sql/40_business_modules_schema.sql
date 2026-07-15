-- 업무/교육 샘플 스키마입니다.
-- 기본 업무 스키마는 XYZ 교육, MBR 회원, BZA 업무 백오피스 주제영역으로 구성합니다.

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

USE bzaDB;

CREATE TABLE IF NOT EXISTS bza_admin_user (
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
    UNIQUE KEY uk_bza_admin_user_login (admin_login_id),
    INDEX ix_bza_admin_user_role (role_code, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 관리자 사용자';

CREATE TABLE IF NOT EXISTS bza_login_history (
    login_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 로그인 이력 순번',
    admin_user_id BIGINT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'BZA' COMMENT '로그인 도메인',
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
    INDEX ix_bza_login_history_user_time (admin_user_id, created_at),
    INDEX ix_bza_login_history_result_time (login_result, created_at),
    INDEX ix_bza_login_history_global (transaction_global_id),
    CONSTRAINT fk_bza_login_history_user
        FOREIGN KEY (admin_user_id) REFERENCES bza_admin_user(admin_user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 관리자 로그인 이력';

CREATE TABLE IF NOT EXISTS bza_refresh_token (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 refresh token 순번',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'BZA' COMMENT '로그인 도메인',
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
    UNIQUE KEY uk_bza_refresh_token_hash (refresh_token_hash),
    INDEX ix_bza_refresh_token_user (admin_user_id, revoked_yn, expire_at),
    CONSTRAINT fk_bza_refresh_token_user
        FOREIGN KEY (admin_user_id) REFERENCES bza_admin_user(admin_user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 관리자 refresh token hash 저장소';

CREATE TABLE IF NOT EXISTS bza_menu (
    menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 메뉴 순번',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    menu_name VARCHAR(120) NOT NULL COMMENT '업무 메뉴명',
    parent_menu_code VARCHAR(80) NULL COMMENT '상위 업무 메뉴 코드',
    module_code VARCHAR(20) NOT NULL DEFAULT 'BZA' COMMENT '소유 업무 모듈 코드',
    route_path VARCHAR(300) NULL COMMENT '화면 이동 경로',
    icon_code VARCHAR(80) NULL COMMENT '화면 아이콘 코드',
    environment_code VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '적용 환경 코드',
    api_path VARCHAR(300) NULL COMMENT '연결 API 경로',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (menu_id),
    UNIQUE KEY uk_bza_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 메뉴';

CREATE TABLE IF NOT EXISTS bza_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 역할 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    role_name VARCHAR(120) NOT NULL COMMENT '업무 역할명',
    write_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '쓰기 허용 여부',
    data_scope VARCHAR(30) NOT NULL DEFAULT 'OWN' COMMENT '기본 데이터 접근 범위',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_bza_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 역할';

CREATE TABLE IF NOT EXISTS bza_permission (
    permission_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 권한 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    button_code VARCHAR(80) NOT NULL COMMENT '버튼/행위 코드',
    permission_type VARCHAR(30) NOT NULL DEFAULT 'BUTTON' COMMENT '권한 유형 SCREEN, BUTTON, API',
    http_method VARCHAR(10) NULL COMMENT 'API HTTP 메서드',
    api_pattern VARCHAR(300) NULL COMMENT 'API 경로 패턴',
    domain_code VARCHAR(30) NULL COMMENT '적용 업무 영역 코드',
    environment_code VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '적용 환경 코드',
    data_scope VARCHAR(30) NOT NULL DEFAULT 'ROLE' COMMENT '권한 데이터 범위',
    allow_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_bza_permission (role_code, menu_code, button_code),
    INDEX ix_bza_permission_menu (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 권한';

CREATE TABLE IF NOT EXISTS bza_organization (
    organization_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '조직 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '조직 코드',
    parent_organization_code VARCHAR(50) NULL COMMENT '상위 조직 코드',
    organization_name VARCHAR(120) NOT NULL COMMENT '조직명',
    organization_type VARCHAR(30) NOT NULL DEFAULT 'DEPARTMENT' COMMENT '조직 유형',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '조직 정렬 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (organization_id),
    UNIQUE KEY uk_bza_organization_code (organization_code),
    INDEX ix_bza_organization_parent (parent_organization_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 조직';

CREATE TABLE IF NOT EXISTS bza_employee (
    employee_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직원 순번',
    employee_no VARCHAR(50) NOT NULL COMMENT '직원 번호',
    admin_user_id BIGINT NULL COMMENT '연결 업무 관리자 사용자 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '소속 조직 코드',
    employee_name VARCHAR(100) NOT NULL COMMENT '직원명',
    position_code VARCHAR(50) NULL COMMENT '직급 코드',
    job_title_code VARCHAR(50) NULL COMMENT '직책 코드',
    manager_employee_no VARCHAR(50) NULL COMMENT '상위 관리자 직원 번호',
    employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '재직 상태',
    join_date DATE NULL COMMENT '입사일',
    leave_date DATE NULL COMMENT '퇴사일',
    email VARCHAR(200) NULL COMMENT '업무 이메일',
    mobile_no VARCHAR(50) NULL COMMENT '업무 휴대폰 번호',
    delegated_approver_no VARCHAR(50) NULL COMMENT '대리 결재자 직원 번호',
    absence_from DATE NULL COMMENT '부재 시작일',
    absence_to DATE NULL COMMENT '부재 종료일',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (employee_id),
    UNIQUE KEY uk_bza_employee_no (employee_no),
    UNIQUE KEY uk_bza_employee_admin_user (admin_user_id),
    INDEX ix_bza_employee_organization (organization_code, employment_status),
    CONSTRAINT fk_bza_employee_admin_user FOREIGN KEY (admin_user_id)
        REFERENCES bza_admin_user(admin_user_id) ON DELETE SET NULL,
    CONSTRAINT fk_bza_employee_organization FOREIGN KEY (organization_code)
        REFERENCES bza_organization(organization_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 직원 프로필';

CREATE TABLE IF NOT EXISTS bza_user_role (
    user_role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 역할 부여 순번',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '권한 적용 시작일시',
    effective_to DATETIME NULL COMMENT '권한 적용 종료일시',
    grant_reason VARCHAR(500) NOT NULL COMMENT '권한 부여 사유',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (user_role_id),
    UNIQUE KEY uk_bza_user_role (admin_user_id, role_code, effective_from),
    INDEX ix_bza_user_role_effective (admin_user_id, use_yn, effective_to),
    CONSTRAINT fk_bza_user_role_user FOREIGN KEY (admin_user_id)
        REFERENCES bza_admin_user(admin_user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 사용자 역할 부여';

CREATE TABLE IF NOT EXISTS bza_business_audit (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 감사 순번',
    transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 전역 거래 ID',
    actor_id VARCHAR(100) NOT NULL COMMENT '처리 사용자 ID',
    action_type VARCHAR(50) NOT NULL COMMENT '업무 행위 유형',
    target_type VARCHAR(80) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(120) NOT NULL COMMENT '대상 ID',
    reason VARCHAR(500) NOT NULL COMMENT '업무 처리 사유',
    before_data LONGTEXT NULL COMMENT '변경 전 데이터',
    after_data LONGTEXT NULL COMMENT '변경 후 데이터',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (audit_id),
    INDEX ix_bza_business_audit_target (target_type, target_id, created_at),
    INDEX ix_bza_business_audit_actor (actor_id, created_at),
    INDEX ix_bza_business_audit_transaction (transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 감사';

CREATE TABLE IF NOT EXISTS bza_notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 알림 순번',
    recipient_login_id VARCHAR(100) NOT NULL COMMENT '수신 BZA 로그인 ID',
    notification_type VARCHAR(40) NOT NULL COMMENT '업무 알림 유형',
    title VARCHAR(200) NOT NULL COMMENT '업무 알림 제목',
    message_body VARCHAR(2000) NOT NULL COMMENT '업무 알림 내용',
    reference_type VARCHAR(80) NULL COMMENT '참조 업무 유형',
    reference_id VARCHAR(120) NULL COMMENT '참조 업무 ID',
    read_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '읽음 여부',
    read_at DATETIME NULL COMMENT '읽음 일시',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (notification_id),
    INDEX ix_bza_notification_recipient (recipient_login_id, read_yn, use_yn, created_at),
    INDEX ix_bza_notification_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 알림';

CREATE TABLE IF NOT EXISTS bza_attachment (
    attachment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '첨부파일 순번',
    attachment_group_id VARCHAR(80) NOT NULL COMMENT '첨부파일 그룹 ID',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명',
    storage_key VARCHAR(500) NOT NULL COMMENT '저장소 상대 key',
    content_type VARCHAR(120) NOT NULL COMMENT '파일 Content-Type',
    file_size BIGINT NOT NULL COMMENT '파일 크기 byte',
    checksum_sha256 CHAR(64) NOT NULL COMMENT '파일 SHA-256 checksum',
    scan_status VARCHAR(40) NOT NULL DEFAULT 'PENDING' COMMENT '보안 검사 상태',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (attachment_id),
    UNIQUE KEY uk_bza_attachment_storage_key (storage_key),
    INDEX ix_bza_attachment_group (attachment_group_id, use_yn, created_at),
    INDEX ix_bza_attachment_checksum (checksum_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 첨부파일 메타';

CREATE TABLE IF NOT EXISTS bza_saved_search (
    saved_search_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '저장 검색 순번',
    owner_login_id VARCHAR(100) NOT NULL COMMENT '저장 검색 소유 로그인 ID',
    screen_code VARCHAR(80) NOT NULL COMMENT '적용 화면 코드',
    search_name VARCHAR(120) NOT NULL COMMENT '저장 검색명',
    criteria_json LONGTEXT NOT NULL COMMENT '검색 조건 JSON',
    shared_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공유 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (saved_search_id),
    UNIQUE KEY uk_bza_saved_search_owner (owner_login_id, screen_code, search_name),
    INDEX ix_bza_saved_search_screen (screen_code, shared_yn, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 저장 검색';

CREATE TABLE IF NOT EXISTS bza_download_audit (
    download_audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '다운로드 감사 순번',
    actor_id VARCHAR(100) NOT NULL COMMENT '다운로드 처리 로그인 ID',
    download_code VARCHAR(80) NOT NULL COMMENT '다운로드 기능 코드',
    reason VARCHAR(500) NOT NULL COMMENT '다운로드 사유',
    filter_json LONGTEXT NULL COMMENT '다운로드 검색 조건 JSON',
    row_count BIGINT NOT NULL DEFAULT 0 COMMENT '다운로드 결과 건수',
    result_status VARCHAR(40) NOT NULL COMMENT '다운로드 결과 상태',
    file_name VARCHAR(255) NULL COMMENT '다운로드 파일명',
    masking_applied_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '마스킹 적용 여부',
    transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 전역 거래 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (download_audit_id),
    INDEX ix_bza_download_audit_actor (actor_id, created_at),
    INDEX ix_bza_download_audit_transaction (transaction_global_id),
    INDEX ix_bza_download_audit_status (result_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 다운로드 감사';

CREATE TABLE IF NOT EXISTS bza_approval_document (
    approval_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 문서 순번',
    approval_no VARCHAR(50) NOT NULL COMMENT '결재 문서 번호',
    approval_type VARCHAR(50) NOT NULL COMMENT '결재 유형',
    business_domain VARCHAR(30) NOT NULL COMMENT '요청 업무 영역',
    title VARCHAR(200) NOT NULL COMMENT '결재 제목',
    requester_employee_no VARCHAR(50) NOT NULL COMMENT '요청자 직원 번호',
    approval_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '결재 상태',
    approval_mode VARCHAR(30) NOT NULL DEFAULT 'SEQUENTIAL' COMMENT '결재 방식',
    current_step_no INT NOT NULL DEFAULT 0 COMMENT '현재 결재 단계',
    due_at DATETIME NULL COMMENT '결재 기한',
    payload_json LONGTEXT NULL COMMENT '결재 업무 데이터 JSON',
    attachment_group_id VARCHAR(100) NULL COMMENT '첨부파일 그룹 ID',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 전역 거래 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (approval_id),
    UNIQUE KEY uk_bza_approval_document_no (approval_no),
    INDEX ix_bza_approval_document_status (approval_status, due_at),
    INDEX ix_bza_approval_document_requester (requester_employee_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 문서';

CREATE TABLE IF NOT EXISTS bza_approval_line (
    approval_line_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재선 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    step_no INT NOT NULL COMMENT '결재 단계',
    approver_employee_no VARCHAR(50) NOT NULL COMMENT '결재자 직원 번호',
    decision_rule VARCHAR(30) NOT NULL DEFAULT 'ALL_APPROVE' COMMENT '단계 승인 규칙',
    decision_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '결재자 결정 상태',
    delegated_from_employee_no VARCHAR(50) NULL COMMENT '위임 원 결재자 직원 번호',
    decision_comment VARCHAR(1000) NULL COMMENT '결재 의견',
    decided_at DATETIME NULL COMMENT '결정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (approval_line_id),
    UNIQUE KEY uk_bza_approval_line (approval_id, step_no, approver_employee_no),
    INDEX ix_bza_approval_line_approver (approver_employee_no, decision_status),
    CONSTRAINT fk_bza_approval_line_document FOREIGN KEY (approval_id)
        REFERENCES bza_approval_document(approval_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재선';

CREATE TABLE IF NOT EXISTS bza_approval_history (
    approval_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 이력 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    action_type VARCHAR(30) NOT NULL COMMENT '결재 행위 유형',
    actor_employee_no VARCHAR(50) NOT NULL COMMENT '처리 직원 번호',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 행위 방지 키',
    reason VARCHAR(500) NOT NULL COMMENT '결재 행위 사유',
    before_status VARCHAR(30) NULL COMMENT '변경 전 상태',
    after_status VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    comment_text VARCHAR(1000) NULL COMMENT '결재 의견',
    transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 전역 거래 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (approval_history_id),
    UNIQUE KEY uk_bza_approval_history_idempotency (idempotency_key),
    INDEX ix_bza_approval_history_document (approval_id, created_at),
    CONSTRAINT fk_bza_approval_history_document FOREIGN KEY (approval_id)
        REFERENCES bza_approval_document(approval_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 상태 변경 이력';

CREATE TABLE IF NOT EXISTS bza_customer (
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
    UNIQUE KEY uk_bza_customer_no (customer_no),
    INDEX ix_bza_customer_status (customer_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 고객';

CREATE TABLE IF NOT EXISTS bza_product (
    product_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 샘플 순번',
    product_code VARCHAR(50) NOT NULL COMMENT '상품 코드',
    product_name VARCHAR(120) NOT NULL COMMENT '상품명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (product_id),
    UNIQUE KEY uk_bza_product_code (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 상품';

CREATE TABLE IF NOT EXISTS bza_order (
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
    UNIQUE KEY uk_bza_order_no (order_no),
    INDEX ix_bza_order_customer (customer_no),
    INDEX ix_bza_order_product (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 주문';

CREATE TABLE IF NOT EXISTS bza_project_setting (
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
    UNIQUE KEY uk_bza_project_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 프로젝트 설정';

CREATE TABLE IF NOT EXISTS bza_masking_audit (
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
    INDEX ix_bza_masking_audit_target (target_type, target_id, created_at),
    INDEX ix_bza_masking_audit_operator (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 마스킹 감사';
