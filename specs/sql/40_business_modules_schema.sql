-- 업무/교육 샘플 스키마입니다.
-- 기본 업무 스키마는 REF 교육, MBR 회원, BZA 업무 백오피스 주제영역으로 구성합니다.

USE refDB;

-- Minimal Transaction Reference Schema Template의 REF 인스턴스입니다.
-- MBR/ACC/Generator 신규 Domain도 Schema/SystemCode/Table prefix만 바꾸고
-- 같은 논리 Column/Constraint 계약을 사용합니다.
CREATE TABLE IF NOT EXISTS ref_sample_item (
    sample_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '샘플 항목 ID',
    sample_key VARCHAR(100) NOT NULL COMMENT '업무 멱등·중복 검증 키',
    item_name VARCHAR(200) NOT NULL COMMENT '최소 업무 데이터명',
    category_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '검색 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    searchable_text VARCHAR(500) NULL COMMENT '검색 검증용 값',
    owner_reference VARCHAR(100) NULL COMMENT '다른 Domain을 직접 조인하지 않는 참조값',
    sort_order BIGINT NOT NULL DEFAULT 0 COMMENT '안정 정렬용 순번',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 거래 추적 ID',
    idempotency_key VARCHAR(100) NULL COMMENT '거래 멱등 키',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_ref_sample_item_key UNIQUE (sample_key),
    CONSTRAINT uk_ref_sample_item_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_ref_sample_item_status CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_ref_sample_item_version CHECK (version_no >= 0),
    CONSTRAINT ck_ref_sample_item_deleted CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_ref_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_ref_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_ref_sample_item_name_sort (item_name, sample_item_id),
    INDEX ix_ref_sample_item_transaction (transaction_global_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='REF Minimal Transaction Reference Sample';

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_target (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    UNIQUE KEY uk_ref_center_cut_sample_target_business (center_cut_job_id, business_key),
    INDEX ix_ref_center_cut_sample_target_status (center_cut_job_id, status_code, business_date),
    INDEX ix_ref_center_cut_sample_target_global (parent_transaction_global_id, child_transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 대상';

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 샘플 결과 순번',
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_ref_center_cut_sample_result_target (target_id),
    INDEX ix_ref_center_cut_sample_result_job (center_cut_job_id, result_status, created_at),
    INDEX ix_ref_center_cut_sample_result_global (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_ref_center_cut_sample_result_target
        FOREIGN KEY (target_id) REFERENCES ref_center_cut_sample_target(target_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 결과';

USE mbrDB;

-- MBR은 회원·인증 업무를 추정하지 않고 CPF 표준 거래 흐름만 검증합니다.
-- REF와 Schema/SystemCode/Table prefix만 다르고 논리 Column/Constraint/Index는 동일합니다.
CREATE TABLE IF NOT EXISTS mbr_sample_item (
    sample_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '샘플 항목 ID',
    sample_key VARCHAR(100) NOT NULL COMMENT '업무 멱등·중복 검증 키',
    item_name VARCHAR(200) NOT NULL COMMENT '최소 업무 데이터명',
    category_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '검색 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    searchable_text VARCHAR(500) NULL COMMENT '검색 검증용 값',
    owner_reference VARCHAR(100) NULL COMMENT '다른 Domain을 직접 조인하지 않는 참조값',
    sort_order BIGINT NOT NULL DEFAULT 0 COMMENT '안정 정렬용 순번',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    transaction_global_id VARCHAR(34) NULL COMMENT 'CPF 거래 추적 ID',
    idempotency_key VARCHAR(100) NULL COMMENT '거래 멱등 키',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_mbr_sample_item_key UNIQUE (sample_key),
    CONSTRAINT uk_mbr_sample_item_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_mbr_sample_item_status CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_mbr_sample_item_version CHECK (version_no >= 0),
    CONSTRAINT ck_mbr_sample_item_deleted CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_mbr_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_mbr_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_mbr_sample_item_name_sort (item_name, sample_item_id),
    INDEX ix_mbr_sample_item_transaction (transaction_global_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='MBR Minimal Transaction Reference Sample';

USE bzaDB;

CREATE TABLE IF NOT EXISTS bza_admin_user (
    admin_user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 사용자 순번',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    admin_name VARCHAR(100) NOT NULL COMMENT '업무 관리자명',
    password_hash VARCHAR(300) NULL COMMENT '업무 관리자 비밀번호 hash',
    role_code VARCHAR(50) NOT NULL COMMENT '호환용 기본 역할 코드; 실제 권한은 bza_user_role 다중 매핑을 정본으로 사용',
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


CREATE TABLE IF NOT EXISTS bza_user_role (
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    valid_from DATETIME(3) NULL COMMENT '역할 적용 시작시각',
    valid_to DATETIME(3) NULL COMMENT '역할 적용 종료시각',
    primary_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '대표 역할 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (admin_user_id, role_code),
    INDEX ix_bza_user_role_role (role_code, valid_to, admin_user_id),
    CONSTRAINT fk_bza_user_role_user FOREIGN KEY (admin_user_id)
        REFERENCES bza_admin_user(admin_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_bza_user_role_role FOREIGN KEY (role_code)
        REFERENCES bza_role(role_code),
    CONSTRAINT ck_bza_user_role_primary CHECK (primary_yn IN ('Y','N')),
    CONSTRAINT ck_bza_user_role_effective CHECK (
        valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 사용자 다중 역할 매핑';

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
    effective_from DATETIME(3) NULL COMMENT '조직 적용 시작시각',
    effective_to DATETIME(3) NULL COMMENT '조직 적용 종료시각',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (organization_id),
    UNIQUE KEY uk_bza_organization_code (organization_code),
    INDEX ix_bza_organization_parent (parent_organization_code, sort_order),
    CONSTRAINT ck_bza_organization_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_bza_organization_effective CHECK (
        effective_to IS NULL OR effective_from IS NULL OR effective_to > effective_from
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 조직';

CREATE TABLE IF NOT EXISTS bza_employee (
    employee_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직원 순번',
    employee_no VARCHAR(50) NOT NULL COMMENT '직원 번호',
    admin_user_id BIGINT NULL COMMENT '연결 업무 관리자 사용자 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '대표 조직 코드; 유효 소속 정본은 bza_employee_assignment',
    employee_name VARCHAR(100) NOT NULL COMMENT '직원명',
    position_code VARCHAR(50) NULL COMMENT '직급 코드',
    job_title_code VARCHAR(50) NULL COMMENT '직책 코드',
    manager_employee_no VARCHAR(50) NULL COMMENT '상위 관리자 직원 번호',
    employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '재직 상태',
    join_date DATE NULL COMMENT '입사일',
    leave_date DATE NULL COMMENT '퇴사일',
    email VARCHAR(200) NULL COMMENT '업무 이메일',
    mobile_no VARCHAR(50) NULL COMMENT '업무 휴대폰 번호',
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
        REFERENCES bza_organization(organization_code),
    CONSTRAINT fk_bza_employee_position FOREIGN KEY (position_code)
        REFERENCES bza_position(position_code) ON DELETE SET NULL,
    CONSTRAINT fk_bza_employee_job_title FOREIGN KEY (job_title_code)
        REFERENCES bza_job_title(job_title_code) ON DELETE SET NULL,
    CONSTRAINT ck_bza_employee_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_bza_employee_employment_period CHECK (
        leave_date IS NULL OR join_date IS NULL OR leave_date >= join_date
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 직원 프로필';


CREATE TABLE IF NOT EXISTS bza_position (
    position_code VARCHAR(50) NOT NULL COMMENT '직급 코드',
    position_name VARCHAR(100) NOT NULL COMMENT '직급명',
    rank_order INT NOT NULL DEFAULT 0 COMMENT '직급 정렬/서열 값',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (position_code),
    CONSTRAINT ck_bza_position_use CHECK (use_yn IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 직급 기준정보';

CREATE TABLE IF NOT EXISTS bza_job_title (
    job_title_code VARCHAR(50) NOT NULL COMMENT '직책 코드',
    job_title_name VARCHAR(100) NOT NULL COMMENT '직책명',
    manager_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '조직 책임자 성격 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (job_title_code),
    CONSTRAINT ck_bza_job_title_flags CHECK (manager_yn IN ('Y','N') AND use_yn IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 직책 기준정보';

CREATE TABLE IF NOT EXISTS bza_employee_assignment (
    assignment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직원 소속/직무 발령 순번',
    employee_no VARCHAR(50) NOT NULL COMMENT '직원 번호',
    organization_code VARCHAR(50) NOT NULL COMMENT '소속 조직 코드',
    position_code VARCHAR(50) NULL COMMENT '직급 코드',
    job_title_code VARCHAR(50) NULL COMMENT '직책 코드',
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT 'PRIMARY/CONCURRENT/SECONDMENT/ACTING',
    primary_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '대표 소속 여부',
    effective_from DATETIME(3) NOT NULL COMMENT '발령 적용 시작시각',
    effective_to DATETIME(3) NULL COMMENT '발령 적용 종료시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (assignment_id),
    INDEX ix_bza_employee_assignment_current (employee_no, effective_to, primary_yn),
    INDEX ix_bza_employee_assignment_org (organization_code, effective_to, job_title_code),
    CONSTRAINT fk_bza_employee_assignment_employee FOREIGN KEY (employee_no)
        REFERENCES bza_employee(employee_no) ON DELETE CASCADE,
    CONSTRAINT fk_bza_employee_assignment_org FOREIGN KEY (organization_code)
        REFERENCES bza_organization(organization_code),
    CONSTRAINT fk_bza_employee_assignment_position FOREIGN KEY (position_code)
        REFERENCES bza_position(position_code) ON DELETE SET NULL,
    CONSTRAINT fk_bza_employee_assignment_job_title FOREIGN KEY (job_title_code)
        REFERENCES bza_job_title(job_title_code) ON DELETE SET NULL,
    CONSTRAINT ck_bza_employee_assignment_type CHECK (
        assignment_type IN ('PRIMARY','CONCURRENT','SECONDMENT','ACTING')
    ),
    CONSTRAINT ck_bza_employee_assignment_primary CHECK (primary_yn IN ('Y','N')),
    CONSTRAINT ck_bza_employee_assignment_effective CHECK (
        effective_to IS NULL OR effective_to > effective_from
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 직원 유효기간 기반 조직/직급/직책 Assignment';

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


CREATE TABLE IF NOT EXISTS bza_approval_policy (
    policy_code VARCHAR(80) NOT NULL COMMENT '업무 결재 정책 코드',
    policy_version INT NOT NULL COMMENT '정책 버전',
    policy_name VARCHAR(150) NOT NULL COMMENT '정책명',
    business_domain VARCHAR(30) NOT NULL COMMENT '적용 업무 영역',
    approval_type VARCHAR(50) NOT NULL COMMENT '적용 결재 유형',
    effective_from DATETIME(3) NOT NULL COMMENT '시행 시작시각',
    effective_to DATETIME(3) NULL COMMENT '시행 종료시각',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    self_approval_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '자기승인 허용 여부',
    description VARCHAR(1000) NULL COMMENT '정책 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (policy_code, policy_version),
    INDEX ix_bza_approval_policy_lookup (business_domain, approval_type, enabled_yn, effective_from, effective_to),
    CONSTRAINT ck_bza_approval_policy_version CHECK (policy_version > 0),
    CONSTRAINT ck_bza_approval_policy_flags CHECK (
        enabled_yn IN ('Y','N') AND self_approval_allowed_yn IN ('Y','N')
    ),
    CONSTRAINT ck_bza_approval_policy_effective CHECK (
        effective_to IS NULL OR effective_to > effective_from
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 결재 정책 Version';

CREATE TABLE IF NOT EXISTS bza_approval_policy_step (
    policy_code VARCHAR(80) NOT NULL COMMENT '업무 결재 정책 코드',
    policy_version INT NOT NULL COMMENT '정책 버전',
    step_no INT NOT NULL COMMENT '결재 단계',
    step_type VARCHAR(30) NOT NULL DEFAULT 'APPROVAL' COMMENT 'APPROVAL/AGREEMENT/REVIEW',
    target_type VARCHAR(30) NOT NULL COMMENT 'EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION',
    target_code VARCHAR(100) NOT NULL COMMENT '대상 코드',
    decision_rule VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY/N_OF_M',
    required_count INT NULL COMMENT 'N_OF_M 최소 결정 수',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 대상 여부',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '동일 단계 표시 순서',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (policy_code, policy_version, step_no, target_type, target_code),
    CONSTRAINT fk_bza_approval_policy_step_policy FOREIGN KEY (policy_code, policy_version)
        REFERENCES bza_approval_policy(policy_code, policy_version) ON DELETE CASCADE,
    CONSTRAINT ck_bza_approval_policy_step_no CHECK (step_no >= 1),
    CONSTRAINT ck_bza_approval_policy_step_type CHECK (step_type IN ('APPROVAL','AGREEMENT','REVIEW')),
    CONSTRAINT ck_bza_approval_policy_step_target CHECK (
        target_type IN ('EMPLOYEE','ROLE','ORGANIZATION','ORG_MANAGER','POSITION')
    ),
    CONSTRAINT ck_bza_approval_policy_step_rule CHECK (decision_rule IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_bza_approval_policy_step_required CHECK (
        required_yn IN ('Y','N')
        AND (
            (decision_rule = 'N_OF_M' AND required_count IS NOT NULL AND required_count > 0)
            OR (decision_rule <> 'N_OF_M' AND required_count IS NULL)
        )
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 결재 정책 단계';

CREATE TABLE IF NOT EXISTS bza_approval_document (
    approval_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 문서 순번',
    approval_no VARCHAR(50) NOT NULL COMMENT '결재 문서 번호',
    approval_type VARCHAR(50) NOT NULL COMMENT '결재 유형',
    business_domain VARCHAR(30) NOT NULL COMMENT '요청 업무 영역',
    policy_code VARCHAR(80) NULL COMMENT '적용 결재 정책 코드',
    policy_version INT NULL COMMENT '적용 결재 정책 버전 Snapshot',
    title VARCHAR(200) NOT NULL COMMENT '결재 제목',
    requester_employee_no VARCHAR(50) NOT NULL COMMENT '요청자 직원 번호',
    requester_organization_code VARCHAR(50) NULL COMMENT '상신 시 요청자 조직 Snapshot',
    requester_position_code VARCHAR(50) NULL COMMENT '상신 시 요청자 직급 Snapshot',
    requester_job_title_code VARCHAR(50) NULL COMMENT '상신 시 요청자 직책 Snapshot',
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
    INDEX ix_bza_approval_document_requester (requester_employee_no, created_at),
    CONSTRAINT fk_bza_approval_document_policy FOREIGN KEY (policy_code, policy_version)
        REFERENCES bza_approval_policy(policy_code, policy_version),
    CONSTRAINT ck_bza_approval_document_policy_pair CHECK (
        (policy_code IS NULL AND policy_version IS NULL)
        OR (policy_code IS NOT NULL AND policy_version IS NOT NULL)
    ),
    CONSTRAINT ck_bza_approval_document_status CHECK (
        approval_status IN ('DRAFT','IN_REVIEW','APPROVED','REJECTED','WITHDRAWN','CANCELED','EXPIRED')
    ),
    CONSTRAINT ck_bza_approval_document_mode CHECK (approval_mode IN ('SEQUENTIAL','PARALLEL')),
    CONSTRAINT ck_bza_approval_document_step CHECK (current_step_no >= 0),
    CONSTRAINT ck_bza_approval_document_version CHECK (version_no >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 문서';

CREATE TABLE IF NOT EXISTS bza_approval_line (
    approval_line_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재선 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    step_no INT NOT NULL COMMENT '결재 단계',
    approver_employee_no VARCHAR(50) NULL COMMENT '직접 직원 대상 호환 필드; 정책 기반 결재는 participant Snapshot 사용',
    step_type VARCHAR(30) NOT NULL DEFAULT 'APPROVAL' COMMENT 'APPROVAL/AGREEMENT/REVIEW',
    target_type VARCHAR(30) NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION',
    target_code VARCHAR(100) NOT NULL COMMENT '정책 Target 코드 Snapshot; EMPLOYEE이면 직원번호',
    target_name_snapshot VARCHAR(150) NULL COMMENT '정책 Target 표시명 Snapshot',
    decision_rule VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY/N_OF_M',
    required_count INT NULL COMMENT 'N_OF_M 최소 결정 수',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 Target 여부',
    decision_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '결재자 결정 상태',
    decision_comment VARCHAR(1000) NULL COMMENT '결재 의견',
    decided_at DATETIME NULL COMMENT '결정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (approval_line_id),
    UNIQUE KEY uk_bza_approval_line (approval_id, step_no, target_type, target_code),
    INDEX ix_bza_approval_line_approver (approver_employee_no, decision_status),
    CONSTRAINT fk_bza_approval_line_document FOREIGN KEY (approval_id)
        REFERENCES bza_approval_document(approval_id) ON DELETE CASCADE,
    CONSTRAINT ck_bza_approval_line_step CHECK (step_no >= 1),
    CONSTRAINT ck_bza_approval_line_step_type CHECK (step_type IN ('APPROVAL','AGREEMENT','REVIEW')),
    CONSTRAINT ck_bza_approval_line_target CHECK (
        target_type IN ('EMPLOYEE','ROLE','ORGANIZATION','ORG_MANAGER','POSITION')
    ),
    CONSTRAINT ck_bza_approval_line_rule CHECK (decision_rule IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_bza_approval_line_required CHECK (
        required_yn IN ('Y','N')
        AND (
            (decision_rule = 'N_OF_M' AND required_count IS NOT NULL AND required_count > 0)
            OR (decision_rule <> 'N_OF_M' AND required_count IS NULL)
        )
    ),
    CONSTRAINT ck_bza_approval_line_status CHECK (
        decision_status IN ('WAITING','APPROVED','AGREED','REJECTED','SKIPPED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재선';


CREATE TABLE IF NOT EXISTS bza_approval_participant (
    approval_participant_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '실제 결재 참여자 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    approval_line_id BIGINT NOT NULL COMMENT '정책 Target/결재선 순번',
    step_no INT NOT NULL COMMENT '결재 단계',
    approver_employee_no VARCHAR(50) NOT NULL COMMENT '상신 시 해석된 실제 결재자',
    organization_code_snapshot VARCHAR(50) NULL COMMENT '결재자 조직 Snapshot',
    position_code_snapshot VARCHAR(50) NULL COMMENT '결재자 직급 Snapshot',
    job_title_code_snapshot VARCHAR(50) NULL COMMENT '결재자 직책 Snapshot',
    delegated_from_employee_no VARCHAR(50) NULL COMMENT '위임 원 결재자',
    decision_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/APPROVED/AGREED/REJECTED/SKIPPED',
    idempotency_key VARCHAR(120) NULL COMMENT '결정 멱등 키',
    decision_comment VARCHAR(1000) NULL COMMENT '결재 의견',
    decided_at DATETIME(3) NULL COMMENT '결정 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (approval_participant_id),
    UNIQUE KEY uk_bza_approval_participant (approval_line_id, approver_employee_no),
    UNIQUE KEY uk_bza_approval_participant_idem (idempotency_key),
    INDEX ix_bza_approval_participant_inbox (approver_employee_no, decision_status, approval_id),
    CONSTRAINT fk_bza_approval_participant_document FOREIGN KEY (approval_id)
        REFERENCES bza_approval_document(approval_id) ON DELETE CASCADE,
    CONSTRAINT fk_bza_approval_participant_line FOREIGN KEY (approval_line_id)
        REFERENCES bza_approval_line(approval_line_id) ON DELETE CASCADE,
    CONSTRAINT ck_bza_approval_participant_step CHECK (step_no >= 1),
    CONSTRAINT ck_bza_approval_participant_status CHECK (
        decision_status IN ('WAITING','APPROVED','AGREED','REJECTED','SKIPPED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 참여자 Snapshot';

CREATE TABLE IF NOT EXISTS bza_approval_delegation (
    delegation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 위임 순번',
    delegator_employee_no VARCHAR(50) NOT NULL COMMENT '위임자 직원 번호',
    delegate_employee_no VARCHAR(50) NOT NULL COMMENT '대결/대리 직원 번호',
    business_domain VARCHAR(30) NULL COMMENT '제한 업무 영역; NULL이면 공통',
    approval_type VARCHAR(50) NULL COMMENT '제한 결재 유형; NULL이면 공통',
    valid_from DATETIME(3) NOT NULL COMMENT '위임 시작시각',
    valid_to DATETIME(3) NOT NULL COMMENT '위임 종료시각',
    reason VARCHAR(500) NOT NULL COMMENT '위임 사유',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (delegation_id),
    INDEX ix_bza_approval_delegation_active (delegator_employee_no, use_yn, valid_from, valid_to),
    CONSTRAINT fk_bza_approval_delegation_from FOREIGN KEY (delegator_employee_no)
        REFERENCES bza_employee(employee_no),
    CONSTRAINT fk_bza_approval_delegation_to FOREIGN KEY (delegate_employee_no)
        REFERENCES bza_employee(employee_no),
    CONSTRAINT ck_bza_approval_delegation_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_bza_approval_delegation_period CHECK (valid_to > valid_from),
    CONSTRAINT ck_bza_approval_delegation_self CHECK (delegator_employee_no <> delegate_employee_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 위임/대결 유효기간';

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
        REFERENCES bza_approval_document(approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 결재 상태 변경 이력';

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

-- ACC는 create-domain 생성기 결과를 실제 CRUD로 검증하는 선택 reference domain입니다.
USE accDB;

CREATE TABLE IF NOT EXISTS acc_account (
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

CREATE TABLE IF NOT EXISTS acc_account_change_log (
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
        REFERENCES acc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACC 계정 변경 감사 이력';
