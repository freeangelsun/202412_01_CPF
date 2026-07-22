-- CPF 트랜잭션 ID/서버 인스턴스 표준과 BIZADM/EXS 주제영역을 보강하는 증분 migration입니다.
-- 신규 설치는 V1 baseline과 00_all_install.sql에 같은 내용이 반영되어 있습니다.

CREATE DATABASE IF NOT EXISTS bizadmDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS exsDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 계정과 권한은 application migration과 분리된 02_create_service_users.sql에서 관리합니다.
-- Flyway 실행 계정에 전역 사용자 생성 권한을 부여하지 않아 최소 권한 원칙을 유지합니다.

USE cpfDB;

ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS SERVER_INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID' AFTER WAS_ID;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS HOST_NAME VARCHAR(120) NULL COMMENT '처리 서버 호스트명' AFTER SERVER_INSTANCE_ID;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS PROCESS_ID VARCHAR(80) NULL COMMENT '처리 서버 프로세스 ID' AFTER HOST_NAME;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS THREAD_NAME VARCHAR(160) NULL COMMENT '처리 스레드명' AFTER PROCESS_ID;
CREATE INDEX IF NOT EXISTS ix_cpf_transaction_log_server_time ON cpf_transaction_log (SERVER_INSTANCE_ID, START_TIME);
USE bizadmDB;

CREATE TABLE IF NOT EXISTS bizadm_admin_user (
    admin_user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 사용자 순번',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    admin_name VARCHAR(100) NOT NULL COMMENT '업무 관리자명',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 관리자 역할 코드',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (admin_user_id),
    UNIQUE KEY uk_bizadm_admin_user_login (admin_login_id),
    INDEX ix_bizadm_admin_user_role (role_code, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BIZADM 업무 관리자 사용자';

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
    token_status VARCHAR(30) NOT NULL COMMENT '토큰 상태',
    expire_at DATETIME NULL COMMENT '토큰 만료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (token_id),
    UNIQUE KEY uk_exs_token_store_key (auth_profile_code, token_key),
    INDEX ix_exs_token_store_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 토큰 저장소';

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
