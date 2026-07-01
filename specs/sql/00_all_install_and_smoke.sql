-- CPF all install SQL.
-- This file contains the full SQL body and does not use SOURCE commands.
-- Rebuild this file from split SQL files with scripts/build-all-install-sql.ps1.
-- ============================================================================
-- specs/sql/01_create_databases.sql
-- ============================================================================
-- CPF 초기 데이터베이스 생성 스크립트입니다.
-- 데이터베이스 생성 권한이 있는 migration 계정 또는 root 계정으로 실행합니다.

CREATE DATABASE IF NOT EXISTS pfwDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS cmnDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS admDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS accDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mbrDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bizadmDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS exsDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
-- ============================================================================
-- CPF table cleanup
-- ============================================================================
-- Recreate the current CPF standard tables for local install and smoke check.

DROP TABLE IF EXISTS exsDB.exs_retry_log;
DROP TABLE IF EXISTS exsDB.exs_control_policy;
DROP TABLE IF EXISTS exsDB.exs_message_log;
DROP TABLE IF EXISTS exsDB.exs_transaction_log;
DROP TABLE IF EXISTS exsDB.exs_route_rule;
DROP TABLE IF EXISTS exsDB.exs_token_event_history;
DROP TABLE IF EXISTS exsDB.exs_token_store;
DROP TABLE IF EXISTS exsDB.exs_auth_profile;
DROP TABLE IF EXISTS exsDB.exs_endpoint;
DROP TABLE IF EXISTS exsDB.exs_channel;
DROP TABLE IF EXISTS exsDB.exs_institution;

DROP TABLE IF EXISTS bizadmDB.bizadm_masking_audit;
DROP TABLE IF EXISTS bizadmDB.bizadm_project_setting;
DROP TABLE IF EXISTS bizadmDB.bizadm_order;
DROP TABLE IF EXISTS bizadmDB.bizadm_product;
DROP TABLE IF EXISTS bizadmDB.bizadm_customer;
DROP TABLE IF EXISTS bizadmDB.bizadm_permission;
DROP TABLE IF EXISTS bizadmDB.bizadm_role;
DROP TABLE IF EXISTS bizadmDB.bizadm_menu;
DROP TABLE IF EXISTS bizadmDB.bizadm_refresh_token;
DROP TABLE IF EXISTS bizadmDB.bizadm_login_history;
DROP TABLE IF EXISTS bizadmDB.bizadm_admin_user;

DROP TABLE IF EXISTS mbrDB.mbr_refresh_token;
DROP TABLE IF EXISTS mbrDB.mbr_member_login_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role;
DROP TABLE IF EXISTS mbrDB.mbr_member;

DROP TABLE IF EXISTS accDB.acc_account;

DROP TABLE IF EXISTS admDB.adm_download_audit_log;
DROP TABLE IF EXISTS admDB.adm_notification_delivery_log;
DROP TABLE IF EXISTS admDB.adm_notification_rule;
DROP TABLE IF EXISTS admDB.adm_operator_session;
DROP TABLE IF EXISTS admDB.adm_login_history;
DROP TABLE IF EXISTS admDB.adm_password_history;
DROP TABLE IF EXISTS admDB.adm_password_policy;
DROP TABLE IF EXISTS admDB.adm_mfa_otp_secret;
DROP TABLE IF EXISTS admDB.adm_ip_allowlist;
DROP TABLE IF EXISTS admDB.adm_audit_log;
DROP TABLE IF EXISTS admDB.adm_role_api_permission;
DROP TABLE IF EXISTS admDB.adm_api_permission;
DROP TABLE IF EXISTS admDB.adm_role_button;
DROP TABLE IF EXISTS admDB.adm_role_menu;
DROP TABLE IF EXISTS admDB.adm_button;
DROP TABLE IF EXISTS admDB.adm_menu;
DROP TABLE IF EXISTS admDB.adm_operator_role;
DROP TABLE IF EXISTS admDB.adm_role;
DROP TABLE IF EXISTS admDB.adm_operator;

DROP TABLE IF EXISTS cmnDB.cmn_edu_query_item;
DROP TABLE IF EXISTS cmnDB.cmn_business_log;
DROP TABLE IF EXISTS cmnDB.cmn_notification_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence_issue_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence;

DROP TABLE IF EXISTS pfwDB.pfw_notification_delivery_log;
DROP TABLE IF EXISTS pfwDB.pfw_notification_rule;
DROP TABLE IF EXISTS pfwDB.pfw_business_day_calendar;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_result;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_item;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_parameter;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_job;
DROP TABLE IF EXISTS pfwDB.pfw_batch_ghost_event;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution_target;
DROP TABLE IF EXISTS pfwDB.pfw_batch_operation_log;
DROP TABLE IF EXISTS pfwDB.pfw_batch_lock;
DROP TABLE IF EXISTS pfwDB.pfw_batch_step_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_job_relation;
DROP TABLE IF EXISTS pfwDB.pfw_batch_worker;
DROP TABLE IF EXISTS pfwDB.pfw_batch_instance;
DROP TABLE IF EXISTS pfwDB.pfw_batch_schedule;
DROP TABLE IF EXISTS pfwDB.pfw_batch_job;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_INSTANCE;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION_SEQ;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_SEQ;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_SEQ;
DROP TABLE IF EXISTS pfwDB.pfw_cache_refresh_event;
DROP TABLE IF EXISTS pfwDB.pfw_dynamic_log_level_rule;
DROP TABLE IF EXISTS pfwDB.pfw_config;
DROP TABLE IF EXISTS pfwDB.pfw_response_code;
DROP TABLE IF EXISTS pfwDB.pfw_message;
DROP TABLE IF EXISTS pfwDB.pfw_code;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log_detail;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log;
-- ============================================================================
-- specs/sql/02_create_service_users.sql
-- ============================================================================
-- CPF 로컬/테스트용 최소 권한 계정 생성 스크립트입니다.
-- 운영 환경에서는 같은 계정 구조를 유지하되 비밀번호는 Vault/KMS 또는 배포 환경변수로 주입합니다.

CREATE USER IF NOT EXISTS 'cpf_pfw_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_cmn_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_bizadm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_exs_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';

CREATE USER IF NOT EXISTS 'cpf_pfw_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_cmn_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_bizadm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_exs_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_cmn_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_bizadm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_exs_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_cmn_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_bizadm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_exs_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON pfwDB.* TO 'cpf_pfw_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cmnDB.* TO 'cpf_cmn_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON admDB.* TO 'cpf_adm_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON accDB.* TO 'cpf_acc_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON mbrDB.* TO 'cpf_mbr_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON bizadmDB.* TO 'cpf_bizadm_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON exsDB.* TO 'cpf_exs_migration'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON pfwDB.* TO 'cpf_pfw_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON cmnDB.* TO 'cpf_cmn_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON admDB.* TO 'cpf_adm_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON accDB.* TO 'cpf_acc_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON mbrDB.* TO 'cpf_mbr_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON bizadmDB.* TO 'cpf_bizadm_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON exsDB.* TO 'cpf_exs_app'@'localhost';

FLUSH PRIVILEGES;
-- ============================================================================
-- specs/sql/10_pfw_schema.sql
-- ============================================================================
-- PFW 프레임워크 엔진 스키마입니다.
-- 거래로그, 시스템 코드/메시지, 응답코드, 설정, 캐시 이벤트, 보안 메타, 배치 운영 메타를 pfwDB에 배치합니다.

USE pfwDB;

CREATE TABLE IF NOT EXISTS pfw_transaction_log (
    LOG_DATE DATE NOT NULL COMMENT '로그 기준일',
    LOG_IDX BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 로그 순번',
    TRANSACTION_ID VARCHAR(100) NULL COMMENT '전역 거래 ID',
    TRACE_ID VARCHAR(100) NULL COMMENT '분산 추적 ID',
    SPAN_ID VARCHAR(100) NULL COMMENT '현재 span ID',
    PARENT_SPAN_ID VARCHAR(100) NULL COMMENT '상위 span ID',
    SEQUENCE_NO INT NULL DEFAULT 1 COMMENT '거래 내부 로그 순번',
    MODULE_ID VARCHAR(20) NULL DEFAULT 'N/A' COMMENT '모듈 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 또는 화면 ID',
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL COMMENT '업무 거래 ID',
    BUSINESS_TRANSACTION_NAME VARCHAR(150) NULL COMMENT '업무 거래명',
    LOG_TYPE VARCHAR(20) NULL DEFAULT 'N/A' COMMENT '로그 유형',
    API_VERSION VARCHAR(20) NULL COMMENT '호출 API 버전',
    CLIENT_APP_ID VARCHAR(80) NULL COMMENT '클라이언트 앱 또는 제휴 시스템 ID',
    CLIENT_VERSION VARCHAR(50) NULL COMMENT '클라이언트 앱 또는 SDK 버전',
    CALLER_SERVICE VARCHAR(120) NULL COMMENT '호출 서비스명',
    CALLER_INSTANCE_ID VARCHAR(120) NULL COMMENT '호출 인스턴스 ID',
    CORRELATION_ID VARCHAR(120) NULL COMMENT '내부 연계 상관관계 ID',
    IDEMPOTENCY_KEY VARCHAR(120) NULL COMMENT '중복 처리 방지 멱등키',
    LOCALE VARCHAR(20) NULL COMMENT '클라이언트 locale',
    TIMEZONE VARCHAR(50) NULL COMMENT '클라이언트 시간대',
    REQUEST_TYPE VARCHAR(20) NULL COMMENT '요청 유형',
    ORIGINAL_CHANNEL_CODE VARCHAR(20) NULL COMMENT '최초 유입 채널 코드',
    CHANNEL_CODE VARCHAR(20) NULL COMMENT '현재 처리 채널 코드',
    MEMBER_NO VARCHAR(50) NULL COMMENT '회원 번호',
    CUSTOMER_NO VARCHAR(50) NULL COMMENT '고객 번호',
    SCREEN_ID VARCHAR(50) NULL COMMENT '화면 ID',
    DEVICE_ID VARCHAR(100) NULL COMMENT '디바이스 ID',
    CLIENT_REQUEST_TIME VARCHAR(30) NULL COMMENT '클라이언트 요청 생성 시각',
    WAS_ID VARCHAR(50) NULL COMMENT '처리 WAS ID',
    SERVER_INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID',
    HOST_NAME VARCHAR(120) NULL COMMENT '처리 서버 호스트명',
    PROCESS_ID VARCHAR(80) NULL COMMENT '처리 서버 프로세스 ID',
    THREAD_NAME VARCHAR(160) NULL COMMENT '처리 스레드명',
    RESERVED_FIELD_1 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 1',
    RESERVED_FIELD_2 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 2',
    RESERVED_FIELD_3 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 3',
    RESERVED_FIELD_4 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 4',
    RESERVED_FIELD_5 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 5',
    HTTP_METHOD VARCHAR(10) NULL COMMENT 'HTTP 메서드',
    URI VARCHAR(500) NULL DEFAULT 'N/A' COMMENT '요청 URI',
    CONTROLLER VARCHAR(255) NULL COMMENT 'Controller 요약',
    EXECUTION_PACKAGE VARCHAR(255) NULL COMMENT '실행 패키지명',
    EXECUTION_CLASS VARCHAR(255) NULL COMMENT '실행 클래스명',
    EXECUTION_METHOD VARCHAR(100) NULL COMMENT '실행 메서드명',
    EXECUTION_SIGNATURE VARCHAR(1000) NULL COMMENT '실행 시그니처',
    WORKFLOW_ID VARCHAR(50) NULL COMMENT '워크플로우 ID',
    WORKFLOW_NAME VARCHAR(100) NULL COMMENT '워크플로우명',
    WORKFLOW_INSTANCE_ID VARCHAR(100) NULL COMMENT '워크플로우 인스턴스 ID',
    WORKFLOW_STEP_ID VARCHAR(50) NULL COMMENT '워크플로우 단계 ID',
    WORKFLOW_STEP_NAME VARCHAR(100) NULL COMMENT '워크플로우 단계명',
    WORKFLOW_STATUS VARCHAR(30) NULL COMMENT '워크플로우 상태',
    WORKFLOW_FAILURE_POLICY VARCHAR(30) NULL COMMENT '워크플로우 실패 정책',
    COMPENSATION_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '보상 거래 여부',
    COMPENSATION_TRANSACTION_ID VARCHAR(20) NULL COMMENT '보상 거래 ID',
    COMPENSATION_TARGET_TRANSACTION_ID VARCHAR(20) NULL COMMENT '보상 대상 거래 ID',
    COMPENSATION_STATUS VARCHAR(30) NULL COMMENT '보상 처리 상태',
    PARAMETERS MEDIUMTEXT NULL COMMENT '마스킹된 요청 파라미터',
    REQUEST_BODY MEDIUMTEXT NULL COMMENT '마스킹된 요청 본문',
    RESPONSE MEDIUMTEXT NULL COMMENT '마스킹된 응답 본문',
    HTTP_STATUS INT NULL COMMENT 'HTTP 상태 코드',
    RESPONSE_CODE VARCHAR(20) NULL COMMENT 'CPF 응답 코드',
    MESSAGE_CODE VARCHAR(20) NULL COMMENT '메시지 코드',
    MESSAGE_CONTENT VARCHAR(1000) NULL COMMENT '외부 호출 메시지',
    ERROR_MESSAGE MEDIUMTEXT NULL COMMENT '마스킹된 오류 메시지',
    ERROR_CODE VARCHAR(100) NULL COMMENT '내부 오류 코드',
    EXTERNAL_MESSAGE VARCHAR(1000) NULL COMMENT '외부 표시 메시지',
    INTERNAL_MESSAGE MEDIUMTEXT NULL COMMENT '내부 진단 메시지',
    EXEC_USER VARCHAR(100) NOT NULL DEFAULT 'N/A' COMMENT '실행 사용자',
    CLIENT_IP VARCHAR(100) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    START_TIME DATETIME(3) NULL COMMENT '처리 시작 시각',
    END_TIME DATETIME(3) NULL COMMENT '처리 종료 시각',
    DURATION_MS BIGINT NULL COMMENT '처리 시간 밀리초',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (LOG_IDX),
    INDEX ix_pfw_transaction_log_date (LOG_DATE),
    INDEX ix_pfw_transaction_log_transaction_id (TRANSACTION_ID),
    INDEX ix_pfw_transaction_log_transaction_time (TRANSACTION_ID, START_TIME, LOG_IDX),
    INDEX ix_pfw_transaction_log_trace_id (TRACE_ID),
    INDEX ix_pfw_transaction_log_business_transaction_id (BUSINESS_TRANSACTION_ID),
    INDEX ix_pfw_transaction_log_business_time (BUSINESS_TRANSACTION_ID, START_TIME),
    INDEX ix_pfw_transaction_log_client_app (CLIENT_APP_ID, START_TIME),
    INDEX ix_pfw_transaction_log_correlation (CORRELATION_ID, START_TIME),
    INDEX ix_pfw_transaction_log_idempotency (IDEMPOTENCY_KEY),
    INDEX ix_pfw_transaction_log_member_time (MEMBER_NO, START_TIME),
    INDEX ix_pfw_transaction_log_customer_time (CUSTOMER_NO, START_TIME),
    INDEX ix_pfw_transaction_log_channel_time (CHANNEL_CODE, START_TIME),
    INDEX ix_pfw_transaction_log_module_time (MODULE_ID, START_TIME),
    INDEX ix_pfw_transaction_log_server_time (SERVER_INSTANCE_ID, START_TIME),
    INDEX ix_pfw_transaction_log_status_time (LOG_TYPE, RESPONSE_CODE, START_TIME),
    INDEX ix_pfw_transaction_log_http_status_time (HTTP_STATUS, START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 거래 요약 로그';

CREATE TABLE IF NOT EXISTS pfw_transaction_log_detail (
    DETAIL_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 상세 로그 순번',
    LOG_IDX BIGINT NOT NULL COMMENT '거래 로그 순번',
    DETAIL_KEY VARCHAR(100) NOT NULL DEFAULT 'N/A' COMMENT '상세 항목 키',
    DETAIL_VALUE MEDIUMTEXT NOT NULL COMMENT '상세 항목 값',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (DETAIL_ID),
    CONSTRAINT fk_pfw_transaction_log_detail_log
        FOREIGN KEY (LOG_IDX) REFERENCES pfw_transaction_log(LOG_IDX)
        ON DELETE CASCADE,
    INDEX ix_pfw_transaction_log_detail_log_idx (LOG_IDX),
    INDEX ix_pfw_transaction_log_detail_log_key (LOG_IDX, DETAIL_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 거래 상세 로그';

CREATE TABLE IF NOT EXISTS pfw_transaction_meta (
    transaction_id VARCHAR(20) NOT NULL COMMENT '업무 거래 ID',
    transaction_name VARCHAR(150) NOT NULL COMMENT '업무 거래명',
    module_code VARCHAR(20) NOT NULL COMMENT '모듈 코드',
    domain_code VARCHAR(50) NULL COMMENT '업무 영역 코드',
    http_method VARCHAR(20) NOT NULL DEFAULT 'ANY' COMMENT 'HTTP 메서드',
    api_path VARCHAR(500) NOT NULL COMMENT 'API 경로',
    controller_class VARCHAR(255) NOT NULL COMMENT 'Controller 클래스명',
    handler_method VARCHAR(150) NOT NULL COMMENT 'Handler 메서드명',
    swagger_operation_id VARCHAR(150) NULL COMMENT 'Swagger operation 식별자',
    log_policy_key VARCHAR(120) NULL COMMENT '연결 로그 정책 키',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감 거래 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '마스킹 정책 키',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    first_detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 감지일시',
    last_detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 감지일시',
    last_scanned_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 스캔일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (transaction_id),
    INDEX ix_pfw_transaction_meta_module (module_code, domain_code, active_yn),
    INDEX ix_pfw_transaction_meta_path (http_method, api_path),
    INDEX ix_pfw_transaction_meta_policy (log_policy_key, active_yn),
    INDEX ix_pfw_transaction_meta_scan (active_yn, last_scanned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 온라인 거래 메타';

CREATE TABLE IF NOT EXISTS pfw_log_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 순번',
    policy_key VARCHAR(120) NOT NULL COMMENT '로그 정책 키',
    policy_name VARCHAR(150) NOT NULL COMMENT '로그 정책명',
    target_type VARCHAR(30) NOT NULL COMMENT '정책 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT '정책 대상 ID',
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '기본 로그 레벨',
    db_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'DB 로그 적재 여부',
    file_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '파일 로그 출력 여부',
    request_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '요청 본문 로그 여부',
    response_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '응답 본문 로그 여부',
    error_stack_log_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '오류 stack 로그 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '마스킹 정책 키',
    retention_days INT NOT NULL DEFAULT 90 COMMENT '보존 일수',
    sampling_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '샘플링 비율',
    priority INT NOT NULL DEFAULT 100 COMMENT '정책 우선순위',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    description VARCHAR(500) NULL COMMENT '정책 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_pfw_log_policy_key (policy_key),
    UNIQUE KEY uk_pfw_log_policy_target (target_type, target_id),
    INDEX ix_pfw_log_policy_active (active_yn, target_type, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책';

CREATE TABLE IF NOT EXISTS pfw_log_policy_override (
    override_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 override 순번',
    policy_id BIGINT NULL COMMENT '기본 로그 정책 순번',
    target_type VARCHAR(30) NOT NULL COMMENT 'override 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT 'override 대상 ID',
    override_reason VARCHAR(500) NOT NULL COMMENT 'override 사유',
    log_level VARCHAR(20) NULL COMMENT '임시 로그 레벨',
    db_log_enabled_yn CHAR(1) NULL COMMENT 'DB 로그 임시 적재 여부',
    file_log_enabled_yn CHAR(1) NULL COMMENT '파일 로그 임시 출력 여부',
    request_body_log_yn CHAR(1) NULL COMMENT '요청 본문 임시 로그 여부',
    response_body_log_yn CHAR(1) NULL COMMENT '응답 본문 임시 로그 여부',
    error_stack_log_yn CHAR(1) NULL COMMENT '오류 stack 임시 로그 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '임시 마스킹 정책 키',
    effective_start_at DATETIME(3) NOT NULL COMMENT '적용 시작일시',
    effective_end_at DATETIME(3) NOT NULL COMMENT '적용 종료일시',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청자',
    approved_by VARCHAR(100) NULL COMMENT '승인자',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (override_id),
    INDEX ix_pfw_log_policy_override_target (target_type, target_id, active_yn),
    INDEX ix_pfw_log_policy_override_period (effective_start_at, effective_end_at, active_yn),
    INDEX ix_pfw_log_policy_override_policy (policy_id, active_yn),
    CONSTRAINT fk_pfw_log_policy_override_policy
        FOREIGN KEY (policy_id) REFERENCES pfw_log_policy(policy_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책 임시 override';

CREATE TABLE IF NOT EXISTS pfw_log_policy_audit (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 감사 순번',
    policy_id BIGINT NULL COMMENT '로그 정책 순번',
    override_id BIGINT NULL COMMENT '로그 정책 override 순번',
    action_type VARCHAR(30) NOT NULL COMMENT '감사 행위 유형',
    target_type VARCHAR(30) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT '대상 ID',
    reason VARCHAR(500) NOT NULL COMMENT '감사 사유',
    before_data MEDIUMTEXT NULL COMMENT '변경 전 데이터',
    after_data MEDIUMTEXT NULL COMMENT '변경 후 데이터',
    diff_data MEDIUMTEXT NULL COMMENT '변경 차이',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    client_ip VARCHAR(100) NULL COMMENT '클라이언트 IP',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (audit_id),
    INDEX ix_pfw_log_policy_audit_target (target_type, target_id, created_at),
    INDEX ix_pfw_log_policy_audit_operator (operator_id, created_at),
    INDEX ix_pfw_log_policy_audit_policy (policy_id, created_at),
    CONSTRAINT fk_pfw_log_policy_audit_policy
        FOREIGN KEY (policy_id) REFERENCES pfw_log_policy(policy_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_log_policy_audit_override
        FOREIGN KEY (override_id) REFERENCES pfw_log_policy_override(override_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책 감사 로그';

CREATE TABLE IF NOT EXISTS pfw_code (
    code_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '코드 순번',
    parent_id BIGINT NULL COMMENT '상위 코드 순번',
    code_key VARCHAR(80) NOT NULL COMMENT '코드 그룹 키',
    code_value VARCHAR(120) NOT NULL COMMENT '코드 값',
    description VARCHAR(500) NULL COMMENT '코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (code_id),
    CONSTRAINT fk_pfw_code_parent
        FOREIGN KEY (parent_id) REFERENCES pfw_code(code_id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_pfw_code_key_value (code_key, code_value),
    INDEX ix_pfw_code_parent (parent_id),
    INDEX ix_pfw_code_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 시스템 공통 코드';

CREATE TABLE IF NOT EXISTS pfw_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '메시지 순번',
    message_code VARCHAR(20) NOT NULL COMMENT '메시지 코드',
    locale VARCHAR(10) NOT NULL DEFAULT 'ko' COMMENT '언어 코드',
    message_format_type VARCHAR(20) NOT NULL DEFAULT 'FIXED' COMMENT '메시지 포맷 유형',
    external_message VARCHAR(2000) NOT NULL COMMENT '외부 노출 메시지',
    internal_message VARCHAR(4000) NOT NULL COMMENT '내부 진단 메시지',
    parameter_count INT NOT NULL DEFAULT 0 COMMENT '파라미터 개수',
    parameter_sample VARCHAR(1000) NULL COMMENT '파라미터 예시',
    description VARCHAR(500) NULL COMMENT '메시지 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (message_id),
    UNIQUE KEY uk_pfw_message_code_locale (message_code, locale),
    INDEX ix_pfw_message_code_use (message_code, use_yn),
    INDEX ix_pfw_message_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 시스템 메시지';

CREATE TABLE IF NOT EXISTS pfw_response_code (
    response_code VARCHAR(20) NOT NULL COMMENT 'CPF 응답 코드',
    message_code VARCHAR(20) NOT NULL COMMENT '연결 메시지 코드',
    result_type CHAR(1) NOT NULL COMMENT '결과 유형',
    module_id VARCHAR(3) NOT NULL COMMENT '모듈 ID',
    response_group VARCHAR(2) NOT NULL COMMENT '응답 그룹',
    sequence_no VARCHAR(4) NOT NULL COMMENT '응답 일련번호',
    http_status INT NOT NULL COMMENT 'HTTP 상태 코드',
    description VARCHAR(500) NULL COMMENT '응답 코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (response_code),
    INDEX ix_pfw_response_code_message (message_code),
    INDEX ix_pfw_response_code_module (module_id, result_type, response_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 응답 코드';

CREATE TABLE IF NOT EXISTS pfw_config (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '설정 순번',
    config_key VARCHAR(150) NOT NULL COMMENT '설정 키',
    config_value VARCHAR(2000) NOT NULL COMMENT '설정 값',
    config_type VARCHAR(30) NOT NULL DEFAULT 'STRING' COMMENT '설정 값 유형',
    description VARCHAR(500) NULL COMMENT '설정 설명',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_pfw_config_key (config_key),
    INDEX ix_pfw_config_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 시스템 설정';

CREATE TABLE IF NOT EXISTS pfw_cache_refresh_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '캐시 갱신 이벤트 순번',
    cache_name VARCHAR(50) NOT NULL COMMENT '캐시 이름',
    event_type VARCHAR(30) NOT NULL COMMENT '이벤트 유형',
    event_key VARCHAR(200) NULL COMMENT '이벤트 대상 키',
    source_was_id VARCHAR(50) NULL COMMENT '이벤트 발행 WAS ID',
    published_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '발행자',
    published_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발행일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (event_id),
    INDEX ix_pfw_cache_refresh_event_cache_id (cache_name, event_id),
    INDEX ix_pfw_cache_refresh_event_time (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 캐시 갱신 DB fallback 이벤트';

CREATE TABLE IF NOT EXISTS pfw_file_exchange_log (
    EXCHANGE_ID VARCHAR(80) NOT NULL COMMENT '파일 교환 ID',
    TRANSACTION_ID VARCHAR(80) NULL COMMENT '전역 거래 ID',
    TRACE_ID VARCHAR(80) NULL COMMENT '분산 추적 ID',
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL COMMENT '업무 거래 ID',
    ACTION_TYPE VARCHAR(30) NOT NULL COMMENT '파일 작업 유형',
    PROTOCOL VARCHAR(20) NOT NULL COMMENT '파일 교환 프로토콜',
    DIRECTION VARCHAR(20) NULL COMMENT '송수신 방향',
    EXECUTED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실행 여부',
    SUCCESS_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '성공 여부',
    HOST VARCHAR(255) NULL COMMENT '대상 호스트',
    SOURCE_PATH VARCHAR(1000) NULL COMMENT '원본 경로',
    TARGET_PATH VARCHAR(1000) NULL COMMENT '대상 경로',
    REQUEST_USER VARCHAR(50) NULL COMMENT '요청 사용자',
    MESSAGE VARCHAR(2000) NULL COMMENT '처리 메시지',
    created_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (EXCHANGE_ID),
    INDEX ix_pfw_file_exchange_tx (TRANSACTION_ID, created_at),
    INDEX ix_pfw_file_exchange_biz (BUSINESS_TRANSACTION_ID, created_at),
    INDEX ix_pfw_file_exchange_host (HOST, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 파일 교환 로그';

CREATE TABLE IF NOT EXISTS pfw_security_jwt_key (
    KEY_ID VARCHAR(80) NOT NULL COMMENT 'JWT key ID',
    ISSUER VARCHAR(100) NOT NULL COMMENT '토큰 발급자',
    ALGORITHM VARCHAR(20) NOT NULL DEFAULT 'HS256' COMMENT '서명 알고리즘',
    SECRET_REF VARCHAR(500) NOT NULL COMMENT 'Vault/KMS/환경변수 secret 참조',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    EXPIRE_AT DATETIME NULL COMMENT '만료일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (KEY_ID),
    INDEX ix_pfw_security_jwt_key_issuer (ISSUER, ACTIVE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW JWT key 메타';

CREATE TABLE IF NOT EXISTS pfw_security_token_audit_log (
    TOKEN_AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 감사 로그 순번',
    TRANSACTION_ID VARCHAR(80) NULL COMMENT '전역 거래 ID',
    TRACE_ID VARCHAR(80) NULL COMMENT '분산 추적 ID',
    TOKEN_HASH VARCHAR(512) NULL COMMENT '토큰 해시',
    TOKEN_TYPE VARCHAR(30) NOT NULL DEFAULT 'Bearer' COMMENT '토큰 유형',
    ISSUER VARCHAR(100) NULL COMMENT '토큰 발급자',
    SUBJECT VARCHAR(200) NULL COMMENT '토큰 주체',
    AUDIENCE VARCHAR(200) NULL COMMENT '토큰 대상',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '활성 여부',
    EXPIRE_AT DATETIME NULL COMMENT '만료일시',
    FAILURE_REASON VARCHAR(1000) NULL COMMENT '검증 실패 사유',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    created_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (TOKEN_AUDIT_ID),
    INDEX ix_pfw_security_token_tx (TRANSACTION_ID),
    INDEX ix_pfw_security_token_hash (TOKEN_HASH),
    INDEX ix_pfw_security_token_subject_time (SUBJECT, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 보안 토큰 감사 로그';

CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_NAME VARCHAR(100) NOT NULL COMMENT 'Spring Batch Job 이름',
    JOB_KEY VARCHAR(32) NOT NULL COMMENT 'Job 파라미터 식별 키',
    PRIMARY KEY (JOB_INSTANCE_ID),
    UNIQUE KEY JOB_INST_UN (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobInstance 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT '실행 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT '실행 상태',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_INST_EXEC_FK
        FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    PARAMETER_NAME VARCHAR(100) NOT NULL COMMENT '파라미터 이름',
    PARAMETER_TYPE VARCHAR(100) NOT NULL COMMENT '파라미터 Java 유형',
    PARAMETER_VALUE VARCHAR(2500) NULL COMMENT '파라미터 값',
    IDENTIFYING CHAR(1) NOT NULL COMMENT 'JobInstance 식별 파라미터 여부',
    CONSTRAINT JOB_EXEC_PARAMS_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 파라미터 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    VERSION BIGINT NOT NULL COMMENT '낙관적 잠금 버전',
    STEP_NAME VARCHAR(100) NOT NULL COMMENT 'Step 이름',
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT 'Step 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT 'Step 상태',
    COMMIT_COUNT BIGINT NULL COMMENT '커밋 횟수',
    READ_COUNT BIGINT NULL COMMENT '읽은 건수',
    FILTER_COUNT BIGINT NULL COMMENT '필터 건수',
    WRITE_COUNT BIGINT NULL COMMENT '쓴 건수',
    READ_SKIP_COUNT BIGINT NULL COMMENT '읽기 skip 건수',
    WRITE_SKIP_COUNT BIGINT NULL COMMENT '쓰기 skip 건수',
    PROCESS_SKIP_COUNT BIGINT NULL COMMENT '처리 skip 건수',
    ROLLBACK_COUNT BIGINT NULL COMMENT 'rollback 건수',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_STEP_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 StepExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT STEP_EXEC_CTX_FK
        FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Step 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_CTX_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch StepExecution 채번 테이블';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch JobExecution 채번 테이블';

CREATE TABLE IF NOT EXISTS BATCH_JOB_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch JobInstance 채번 테이블';

CREATE TABLE IF NOT EXISTS pfw_batch_job (
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_name VARCHAR(150) NOT NULL COMMENT '배치 Job 이름',
    job_type VARCHAR(30) NOT NULL DEFAULT 'TASKLET' COMMENT '배치 Job 유형',
    description VARCHAR(500) NULL COMMENT '배치 설명',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (job_id),
    INDEX ix_pfw_batch_job_use (use_yn, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 Job 기준';

CREATE TABLE IF NOT EXISTS pfw_batch_schedule (
    schedule_id VARCHAR(100) NOT NULL COMMENT '배치 스케줄 ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron 표현식',
    calendar_id VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '적용 영업일 캘린더 ID',
    business_day_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '영업일에만 수행 여부',
    holiday_policy VARCHAR(30) NOT NULL DEFAULT 'SKIP' COMMENT '휴일 처리 정책',
    available_start_time TIME NULL COMMENT '수행 가능 시작 시각',
    available_end_time TIME NULL COMMENT '수행 가능 종료 시각',
    run_date_pattern VARCHAR(80) NULL COMMENT '수행 일자 패턴',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '스케줄 기준 시간대',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '스케줄 활성 여부',
    last_fire_at DATETIME NULL COMMENT '마지막 실행 예정 일시',
    next_fire_at DATETIME NULL COMMENT '다음 실행 예정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (schedule_id),
    INDEX ix_pfw_batch_schedule_job (job_id, enabled_yn),
    CONSTRAINT fk_pfw_batch_schedule_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 스케줄';

CREATE TABLE IF NOT EXISTS pfw_batch_job_relation (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 관계 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '기준 배치 Job ID',
    related_job_id VARCHAR(100) NOT NULL COMMENT '연관 배치 Job ID',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 유형',
    trigger_condition VARCHAR(50) NOT NULL DEFAULT 'COMPLETED' COMMENT '트리거 조건',
    required_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT '필수 선행 상태',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '관계 표시 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_pfw_batch_job_relation (job_id, related_job_id, relation_type),
    INDEX ix_pfw_batch_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_pfw_batch_job_relation_related (related_job_id, relation_type),
    CONSTRAINT fk_pfw_batch_job_relation_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pfw_batch_job_relation_related
        FOREIGN KEY (related_job_id) REFERENCES pfw_batch_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 선행/후행/트리거 관계';

CREATE TABLE IF NOT EXISTS pfw_batch_instance (
    instance_id VARCHAR(100) NOT NULL COMMENT '배치 인스턴스 ID',
    instance_name VARCHAR(150) NOT NULL COMMENT '배치 인스턴스명',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    server_port INT NULL COMMENT '서버 포트',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    description VARCHAR(500) NULL COMMENT '인스턴스 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (instance_id),
    INDEX ix_pfw_batch_instance_active (active_yn, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 서버 인스턴스';

CREATE TABLE IF NOT EXISTS pfw_batch_worker (
    worker_id VARCHAR(160) NOT NULL COMMENT '배치 worker ID',
    server_instance_id VARCHAR(160) NOT NULL COMMENT '서버 인스턴스 ID',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    process_id VARCHAR(80) NULL COMMENT '프로세스 ID',
    thread_name VARCHAR(160) NULL COMMENT '스레드명',
    worker_status VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT 'worker 상태',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    current_job_id VARCHAR(100) NULL COMMENT '현재 실행 Job ID',
    current_execution_id BIGINT NULL COMMENT '현재 CPF 배치 실행 순번',
    description VARCHAR(500) NULL COMMENT 'worker 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (worker_id),
    INDEX ix_pfw_batch_worker_server (server_instance_id, active_yn),
    INDEX ix_pfw_batch_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_pfw_batch_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 worker heartbeat';

CREATE TABLE IF NOT EXISTS pfw_batch_execution (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    job_parameters VARCHAR(2000) NULL COMMENT '배치 파라미터',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT '실행 메타 마지막 heartbeat 일시',
    current_step_name VARCHAR(150) NULL COMMENT '현재 실행 중인 Step 이름',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    requested_by VARCHAR(100) NULL COMMENT '실행 요청자',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (execution_id),
    INDEX ix_pfw_batch_execution_job_time (job_id, start_time),
    INDEX ix_pfw_batch_execution_status (execution_status, start_time),
    INDEX ix_pfw_batch_execution_spring (spring_batch_execution_id),
    INDEX ix_pfw_batch_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_pfw_batch_execution_transaction (transaction_global_id),
    INDEX ix_pfw_batch_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_pfw_batch_execution_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id),
    CONSTRAINT fk_pfw_batch_execution_instance
        FOREIGN KEY (batch_instance_id) REFERENCES pfw_batch_instance(instance_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_batch_execution_worker
        FOREIGN KEY (worker_id) REFERENCES pfw_batch_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 실행 이력';

CREATE TABLE IF NOT EXISTS pfw_batch_execution_target (
    target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 수행 대상 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    target_instance_id VARCHAR(100) NULL COMMENT '수행 대상 인스턴스 ID',
    business_date DATE NULL COMMENT '업무 기준일',
    planned_run_at DATETIME(3) NULL COMMENT '예정 수행 일시',
    dispatch_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '배정 상태',
    dispatch_reason VARCHAR(500) NULL COMMENT '배정 또는 제외 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    INDEX ix_pfw_batch_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_pfw_batch_execution_target_execution (execution_id),
    INDEX ix_pfw_batch_execution_target_instance (target_instance_id, dispatch_status),
    CONSTRAINT fk_pfw_batch_execution_target_execution
        FOREIGN KEY (execution_id) REFERENCES pfw_batch_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_batch_execution_target_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id),
    CONSTRAINT fk_pfw_batch_execution_target_schedule
        FOREIGN KEY (schedule_id) REFERENCES pfw_batch_schedule(schedule_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_batch_execution_target_instance
        FOREIGN KEY (target_instance_id) REFERENCES pfw_batch_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 수행 대상/대기 인스턴스';

CREATE TABLE IF NOT EXISTS pfw_batch_step_execution (
    step_execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 Step 실행 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    spring_batch_step_execution_id BIGINT NULL COMMENT 'Spring Batch StepExecution ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    step_name VARCHAR(150) NOT NULL COMMENT 'Step 이름',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT 'Step 메타 마지막 heartbeat 일시',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    step_log MEDIUMTEXT NULL COMMENT 'Step 로그',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (step_execution_id),
    INDEX ix_pfw_batch_step_execution_parent (execution_id, step_name),
    INDEX ix_pfw_batch_step_execution_spring (spring_batch_step_execution_id),
    INDEX ix_pfw_batch_step_execution_worker (worker_id, start_time),
    INDEX ix_pfw_batch_step_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_pfw_batch_step_execution_parent
        FOREIGN KEY (execution_id) REFERENCES pfw_batch_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pfw_batch_step_execution_worker
        FOREIGN KEY (worker_id) REFERENCES pfw_batch_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 Step 실행 이력';

CREATE TABLE IF NOT EXISTS pfw_batch_lock (
    lock_key VARCHAR(200) NOT NULL COMMENT '배치 잠금 키',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_parameters_hash VARCHAR(128) NOT NULL COMMENT 'Job 파라미터 해시',
    owner_id VARCHAR(100) NOT NULL COMMENT '잠금 소유자',
    locked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '잠금 획득 일시',
    expire_at DATETIME(3) NOT NULL COMMENT '잠금 만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (lock_key),
    INDEX ix_pfw_batch_lock_job (job_id, job_parameters_hash),
    INDEX ix_pfw_batch_lock_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 중복 실행 방지 잠금';

CREATE TABLE IF NOT EXISTS pfw_batch_operation_log (
    operation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (operation_id),
    INDEX ix_pfw_batch_operation_job_time (job_id, created_at),
    INDEX ix_pfw_batch_operation_execution (execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 운영 작업 로그';

CREATE TABLE IF NOT EXISTS pfw_batch_ghost_event (
    ghost_event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 ghost 이벤트 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    server_instance_id VARCHAR(160) NULL COMMENT '서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT 'worker ID',
    ghost_status VARCHAR(30) NOT NULL DEFAULT 'DETECTED' COMMENT 'ghost 이벤트 상태',
    detected_reason VARCHAR(1000) NOT NULL COMMENT '감지 사유',
    action_type VARCHAR(30) NULL COMMENT '조치 유형',
    action_reason VARCHAR(1000) NULL COMMENT '조치 사유',
    action_by VARCHAR(100) NULL COMMENT '조치 운영자',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '감지 일시',
    action_at DATETIME(3) NULL COMMENT '조치 일시',
    lock_released_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 해제 여부',
    retryable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재수행 가능 여부',
    before_data LONGTEXT NULL COMMENT '조치 전 데이터',
    after_data LONGTEXT NULL COMMENT '조치 후 데이터',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ghost_event_id),
    INDEX ix_pfw_batch_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_pfw_batch_ghost_event_job (job_id, detected_at),
    INDEX ix_pfw_batch_ghost_event_worker (worker_id, detected_at),
    CONSTRAINT fk_pfw_batch_ghost_event_execution
        FOREIGN KEY (execution_id) REFERENCES pfw_batch_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_batch_ghost_event_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id),
    CONSTRAINT fk_pfw_batch_ghost_event_worker
        FOREIGN KEY (worker_id) REFERENCES pfw_batch_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 ghost 감지와 조치 이력';

CREATE TABLE IF NOT EXISTS pfw_center_cut_job (
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    batch_job_id VARCHAR(100) NULL COMMENT '연결된 PFW 배치 Job ID',
    center_cut_job_name VARCHAR(150) NOT NULL COMMENT '센터컷 Job 명',
    provider_key VARCHAR(100) NOT NULL COMMENT '대상 조회 Provider 식별자',
    handler_key VARCHAR(100) NOT NULL COMMENT '처리 Handler 식별자',
    chunk_size INT NOT NULL DEFAULT 100 COMMENT '한 번에 조회할 대상 건수',
    retry_limit INT NOT NULL DEFAULT 3 COMMENT '최대 재처리 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    description VARCHAR(500) NULL COMMENT '센터컷 Job 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_job_id),
    INDEX ix_pfw_center_cut_job_batch (batch_job_id, use_yn),
    CONSTRAINT fk_pfw_center_cut_job_batch
        FOREIGN KEY (batch_job_id) REFERENCES pfw_batch_job(job_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 센터컷 Job 정의';

CREATE TABLE IF NOT EXISTS pfw_center_cut_parameter (
    parameter_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 파라미터 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    parameter_key VARCHAR(100) NOT NULL COMMENT '파라미터 키',
    parameter_value VARCHAR(1000) NULL COMMENT '파라미터 값',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (parameter_id),
    UNIQUE KEY uk_pfw_center_cut_parameter (center_cut_job_id, parameter_key),
    CONSTRAINT fk_pfw_center_cut_parameter_job
        FOREIGN KEY (center_cut_job_id) REFERENCES pfw_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 센터컷 파라미터';

CREATE TABLE IF NOT EXISTS pfw_center_cut_item (
    center_cut_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등성 키',
    business_date DATE NULL COMMENT '업무 기준일',
    item_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    item_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_item_id),
    UNIQUE KEY uk_pfw_center_cut_item_business (center_cut_job_id, business_key),
    INDEX ix_pfw_center_cut_item_status (center_cut_job_id, item_status, business_date),
    INDEX ix_pfw_center_cut_item_transaction (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_pfw_center_cut_item_job
        FOREIGN KEY (center_cut_job_id) REFERENCES pfw_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 센터컷 처리 대상';

CREATE TABLE IF NOT EXISTS pfw_center_cut_result (
    center_cut_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 결과 순번',
    center_cut_item_id BIGINT NOT NULL COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_result_id),
    INDEX ix_pfw_center_cut_result_item (center_cut_item_id, result_status),
    INDEX ix_pfw_center_cut_result_job (center_cut_job_id, created_at),
    CONSTRAINT fk_pfw_center_cut_result_item
        FOREIGN KEY (center_cut_item_id) REFERENCES pfw_center_cut_item(center_cut_item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pfw_center_cut_result_job
        FOREIGN KEY (center_cut_job_id) REFERENCES pfw_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 센터컷 처리 결과';

CREATE TABLE IF NOT EXISTS pfw_business_day_calendar (
    calendar_id VARCHAR(50) NOT NULL COMMENT '캘린더 ID',
    business_date DATE NOT NULL COMMENT '기준 일자',
    holiday_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '휴일 여부',
    business_day_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '영업일 여부',
    description VARCHAR(500) NULL COMMENT '일자 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (calendar_id, business_date),
    INDEX ix_pfw_business_day_calendar_date (business_date, business_day_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 영업일 캘린더';

CREATE TABLE IF NOT EXISTS pfw_notification_rule (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    event_sub_type VARCHAR(80) NULL COMMENT '알림 이벤트 세부 유형',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ADM' COMMENT '알림 채널 코드',
    template_code VARCHAR(80) NULL COMMENT '알림 템플릿 코드',
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '알림 심각도',
    receiver_group VARCHAR(100) NULL COMMENT '수신자 그룹',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_pfw_notification_rule (event_type, event_sub_type, channel_code),
    INDEX ix_pfw_notification_rule_use (use_yn, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 운영 알림 규칙';

CREATE TABLE IF NOT EXISTS pfw_notification_delivery_log (
    delivery_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 발송 로그 순번',
    rule_id BIGINT NULL COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    target_type VARCHAR(80) NULL COMMENT '알림 대상 유형',
    target_id VARCHAR(120) NULL COMMENT '알림 대상 ID',
    receiver VARCHAR(200) NULL COMMENT '수신자',
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '발송 상태',
    delivery_message VARCHAR(2000) NULL COMMENT '발송 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발송 요청 일시',
    delivered_at DATETIME(3) NULL COMMENT '발송 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (delivery_id),
    INDEX ix_pfw_notification_delivery_target (target_type, target_id, requested_at),
    INDEX ix_pfw_notification_delivery_status (delivery_status, requested_at),
    CONSTRAINT fk_pfw_notification_delivery_rule
        FOREIGN KEY (rule_id) REFERENCES pfw_notification_rule(rule_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 운영 알림 발송 로그';
-- ============================================================================
-- specs/sql/20_cmn_schema.sql
-- ============================================================================
-- CMN 업무 공통 스키마입니다.
-- 여러 업무 모듈이 함께 사용하는 채번, 알림 로그, 업무 로그를 cmnDB에 배치합니다.

USE cmnDB;

CREATE TABLE IF NOT EXISTS cmn_sequence (
    sequence_key VARCHAR(80) NOT NULL COMMENT '채번 기준 키',
    business_area VARCHAR(50) NOT NULL DEFAULT 'COMMON' COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL DEFAULT 'DEFAULT' COMMENT '업무 키',
    sequence_kind VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '채번 종류',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT '채널 코드',
    prefix VARCHAR(30) NOT NULL COMMENT '채번 접두어',
    date_pattern VARCHAR(20) NULL COMMENT '번호에 포함할 일자 패턴',
    current_value BIGINT NOT NULL DEFAULT 0 COMMENT '현재 채번 값',
    start_value BIGINT NOT NULL DEFAULT 1 COMMENT '초기 시작 번호',
    increment_by INT NOT NULL DEFAULT 1 COMMENT '증가 단위',
    min_value BIGINT NOT NULL DEFAULT 1 COMMENT '허용 최소 번호',
    max_value BIGINT NOT NULL DEFAULT 999999999999999999 COMMENT '허용 최대 번호',
    range_size INT NOT NULL DEFAULT 1 COMMENT '향후 구간 선점 확장을 위한 예약 크기',
    number_length INT NOT NULL DEFAULT 8 COMMENT '번호 숫자 영역 길이',
    reset_cycle VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '초기화 주기',
    reset_pattern VARCHAR(20) NULL COMMENT '초기화 기준 일자 패턴',
    reset_timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '초기화 기준 시간대',
    last_reset_key VARCHAR(20) NULL COMMENT '마지막 초기화 기준 키',
    log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '발급 로그 저장 여부',
    retention_days INT NOT NULL DEFAULT 365 COMMENT '발급 로그 보존 일수',
    description VARCHAR(500) NULL COMMENT '채번 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (sequence_key),
    UNIQUE KEY uk_cmn_sequence_business (business_area, business_key, sequence_kind, channel_code),
    INDEX ix_cmn_sequence_use (use_yn),
    INDEX ix_cmn_sequence_reset (reset_cycle, last_reset_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 채번 기준';

CREATE TABLE IF NOT EXISTS cmn_sequence_issue_log (
    issue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '채번 발급 로그 순번',
    sequence_key VARCHAR(80) NOT NULL COMMENT '채번 기준 키',
    business_area VARCHAR(50) NOT NULL DEFAULT 'COMMON' COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL DEFAULT 'DEFAULT' COMMENT '업무 키',
    sequence_kind VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '채번 종류',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT '채널 코드',
    issued_no VARCHAR(120) NOT NULL COMMENT '최종 발급 번호',
    issued_value BIGINT NOT NULL COMMENT '발급 숫자 값',
    prefix VARCHAR(30) NOT NULL COMMENT '발급 시점 접두어',
    date_key VARCHAR(20) NULL COMMENT '발급 시점 일자 키',
    request_channel VARCHAR(30) NULL COMMENT '요청 채널',
    request_user VARCHAR(100) NULL COMMENT '요청 사용자',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    success_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '발급 성공 여부',
    failure_reason VARCHAR(1000) NULL COMMENT '발급 실패 사유',
    retention_until DATE NULL COMMENT '보존 만료 기준일',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_cmn_sequence_issue_no (issued_no),
    INDEX ix_cmn_sequence_issue_key_time (sequence_key, created_at),
    INDEX ix_cmn_sequence_issue_business_time (business_area, business_key, sequence_kind, channel_code, created_at),
    INDEX ix_cmn_sequence_issue_retention (retention_until),
    CONSTRAINT fk_cmn_sequence_issue_sequence
        FOREIGN KEY (sequence_key) REFERENCES cmn_sequence(sequence_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 채번 발급 로그';

CREATE TABLE IF NOT EXISTS cmn_notification_log (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 로그 순번',
    notification_type VARCHAR(30) NOT NULL COMMENT '알림 유형',
    receiver VARCHAR(200) NOT NULL COMMENT '수신자',
    title VARCHAR(300) NOT NULL COMMENT '알림 제목',
    message TEXT NOT NULL COMMENT '알림 메시지',
    send_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '발송 상태',
    send_result VARCHAR(1000) NULL COMMENT '발송 결과',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (notification_id),
    INDEX ix_cmn_notification_status_time (send_status, created_at),
    INDEX ix_cmn_notification_receiver_time (receiver, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 알림 로그';

CREATE TABLE IF NOT EXISTS cmn_business_log (
    business_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 공통 로그 순번',
    business_area VARCHAR(30) NOT NULL COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL COMMENT '업무 키',
    log_type VARCHAR(30) NOT NULL COMMENT '로그 유형',
    log_message VARCHAR(2000) NOT NULL COMMENT '로그 메시지',
    log_payload LONGTEXT NULL COMMENT '로그 상세 payload',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (business_log_id),
    INDEX ix_cmn_business_log_area_key (business_area, business_key),
    INDEX ix_cmn_business_log_type_time (log_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 업무 로그';
CREATE TABLE IF NOT EXISTS cmn_edu_query_item (
    item_id BIGINT NOT NULL COMMENT '교육 조회 항목 ID',
    item_name VARCHAR(200) NOT NULL COMMENT '교육 조회 항목명',
    category_code VARCHAR(30) NOT NULL COMMENT '교육 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    owner_member_no VARCHAR(50) NULL COMMENT '예시 담당 회원 번호',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (item_id),
    INDEX ix_cmn_edu_query_item_search (status_code, category_code, item_name),
    INDEX ix_cmn_edu_query_item_created (created_at, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN EDU 조회 샘플 항목';
-- ============================================================================
-- specs/sql/30_adm_schema.sql
-- ============================================================================
-- ADM 관리자 운영 스키마입니다.
-- 운영자, 역할, 메뉴/버튼 권한, 세션, 감사 로그, 보안 운영 메타를 admDB에 배치합니다.

USE admDB;

CREATE TABLE IF NOT EXISTS adm_operator (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    OPERATOR_NAME VARCHAR(100) NOT NULL COMMENT '운영자명',
    PASSWORD_HASH VARCHAR(512) NOT NULL COMMENT '비밀번호 해시',
    LOCKED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부',
    FAIL_COUNT INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    PASSWORD_CHANGED_AT DATETIME NULL COMMENT '비밀번호 변경일시',
    PASSWORD_EXPIRE_AT DATETIME NULL COMMENT '비밀번호 만료일시',
    PASSWORD_CHANGE_REQUIRED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '비밀번호 변경 필요 여부',
    LAST_LOGIN_AT DATETIME NULL COMMENT '마지막 로그인 일시',
    LAST_LOGIN_IP VARCHAR(50) NULL COMMENT '마지막 로그인 IP',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (OPERATOR_ID),
    INDEX ix_adm_operator_use (USE_YN),
    INDEX ix_adm_operator_lock (LOCKED_YN, FAIL_COUNT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자';

CREATE TABLE IF NOT EXISTS adm_role (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    ROLE_NAME VARCHAR(100) NOT NULL COMMENT '역할명',
    ROLE_TYPE VARCHAR(30) NOT NULL DEFAULT 'BUSINESS_OPERATOR' COMMENT '역할 유형',
    DESCRIPTION VARCHAR(500) NULL COMMENT '역할 설명',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ROLE_ID),
    INDEX ix_adm_role_type (ROLE_TYPE, USE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할';

CREATE TABLE IF NOT EXISTS adm_operator_role (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (OPERATOR_ID, ROLE_ID),
    CONSTRAINT fk_adm_operator_role_operator
        FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator(OPERATOR_ID)
        ON DELETE CASCADE,
    CONSTRAINT fk_adm_operator_role_role
        FOREIGN KEY (ROLE_ID) REFERENCES adm_role(ROLE_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 역할 매핑';

CREATE TABLE IF NOT EXISTS adm_menu (
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    PARENT_MENU_ID VARCHAR(50) NULL COMMENT '상위 메뉴 ID',
    MENU_NAME VARCHAR(100) NOT NULL COMMENT '메뉴명',
    MENU_PATH VARCHAR(200) NOT NULL COMMENT '메뉴 경로',
    SORT_ORDER INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (MENU_ID),
    INDEX ix_adm_menu_parent (PARENT_MENU_ID, SORT_ORDER),
    CONSTRAINT fk_adm_menu_parent
        FOREIGN KEY (PARENT_MENU_ID) REFERENCES adm_menu(MENU_ID)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 메뉴';

CREATE TABLE IF NOT EXISTS adm_role_menu (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    READ_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '조회 권한 여부',
    WRITE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '등록/수정 권한 여부',
    DELETE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 권한 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ROLE_ID, MENU_ID),
    CONSTRAINT fk_adm_role_menu_role
        FOREIGN KEY (ROLE_ID) REFERENCES adm_role(ROLE_ID)
        ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_menu_menu
        FOREIGN KEY (MENU_ID) REFERENCES adm_menu(MENU_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 메뉴 권한';

CREATE TABLE IF NOT EXISTS adm_button (
    BUTTON_ID VARCHAR(80) NOT NULL COMMENT '버튼/행위 ID',
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    ACTION_CODE VARCHAR(50) NOT NULL COMMENT '행위 코드',
    BUTTON_NAME VARCHAR(100) NOT NULL COMMENT '버튼/행위명',
    HTTP_METHOD VARCHAR(10) NULL COMMENT '대상 HTTP 메서드',
    API_PATTERN VARCHAR(300) NULL COMMENT '대상 API 경로 패턴',
    SORT_ORDER INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (BUTTON_ID),
    UNIQUE KEY uk_adm_button_menu_action (MENU_ID, ACTION_CODE),
    INDEX ix_adm_button_menu (MENU_ID, SORT_ORDER),
    CONSTRAINT fk_adm_button_menu
        FOREIGN KEY (MENU_ID) REFERENCES adm_menu(MENU_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 메뉴별 버튼/행위';

CREATE TABLE IF NOT EXISTS adm_role_button (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    BUTTON_ID VARCHAR(80) NOT NULL COMMENT '버튼/행위 ID',
    ALLOW_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ROLE_ID, BUTTON_ID),
    CONSTRAINT fk_adm_role_button_role
        FOREIGN KEY (ROLE_ID) REFERENCES adm_role(ROLE_ID)
        ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_button_button
        FOREIGN KEY (BUTTON_ID) REFERENCES adm_button(BUTTON_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 버튼/행위 권한';

CREATE TABLE IF NOT EXISTS adm_api_permission (
    API_PERMISSION_ID VARCHAR(120) NOT NULL COMMENT 'API 권한 ID',
    API_GROUP_CODE VARCHAR(50) NOT NULL COMMENT 'API 그룹 코드',
    HTTP_METHOD VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    API_PATH VARCHAR(300) NOT NULL COMMENT 'API 경로 패턴',
    API_NAME VARCHAR(150) NOT NULL COMMENT 'API명',
    PERMISSION_CODE VARCHAR(50) NOT NULL COMMENT '권한 코드',
    MENU_ID VARCHAR(50) NULL COMMENT '연결 메뉴 ID',
    BUTTON_ID VARCHAR(80) NULL COMMENT '연결 버튼/행위 ID',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (API_PERMISSION_ID),
    UNIQUE KEY uk_adm_api_permission_method_path (HTTP_METHOD, API_PATH),
    INDEX ix_adm_api_permission_group (API_GROUP_CODE, USE_YN),
    INDEX ix_adm_api_permission_menu (MENU_ID, BUTTON_ID),
    CONSTRAINT fk_adm_api_permission_menu
        FOREIGN KEY (MENU_ID) REFERENCES adm_menu(MENU_ID)
        ON DELETE SET NULL,
    CONSTRAINT fk_adm_api_permission_button
        FOREIGN KEY (BUTTON_ID) REFERENCES adm_button(BUTTON_ID)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM API 권한';

CREATE TABLE IF NOT EXISTS adm_role_api_permission (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    API_PERMISSION_ID VARCHAR(120) NOT NULL COMMENT 'API 권한 ID',
    ALLOW_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ROLE_ID, API_PERMISSION_ID),
    CONSTRAINT fk_adm_role_api_permission_role
        FOREIGN KEY (ROLE_ID) REFERENCES adm_role(ROLE_ID)
        ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_api_permission_api
        FOREIGN KEY (API_PERMISSION_ID) REFERENCES adm_api_permission(API_PERMISSION_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 API 권한';

CREATE TABLE IF NOT EXISTS adm_audit_log (
    AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '감사 로그 순번',
    TRANSACTION_ID VARCHAR(80) NULL COMMENT '프레임워크 거래 ID',
    TRACE_ID VARCHAR(80) NULL COMMENT '분산 추적 ID',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 ID',
    BUTTON_ID VARCHAR(80) NULL COMMENT '버튼/행위 ID',
    ACTION_TYPE VARCHAR(30) NOT NULL COMMENT '행위 유형',
    TARGET_TYPE VARCHAR(50) NULL COMMENT '대상 유형',
    TARGET_ID VARCHAR(100) NULL COMMENT '대상 ID',
    REASON VARCHAR(500) NOT NULL COMMENT '감사 사유',
    BEFORE_DATA LONGTEXT NULL COMMENT '변경 전 데이터',
    AFTER_DATA LONGTEXT NULL COMMENT '변경 후 데이터',
    DIFF_DATA LONGTEXT NULL COMMENT '변경 차이 데이터',
    REQUEST_BODY LONGTEXT NULL COMMENT '요청 본문',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    RETENTION_UNTIL DATE NULL COMMENT '보존 만료 기준일',
    IMMUTABLE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '삭제 불가 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (AUDIT_ID),
    INDEX ix_adm_audit_log_tx (TRANSACTION_ID),
    INDEX ix_adm_audit_log_operator_time (OPERATOR_ID, created_at),
    INDEX ix_adm_audit_log_action_time (ACTION_TYPE, created_at),
    INDEX ix_adm_audit_log_target_time (TARGET_TYPE, TARGET_ID, created_at),
    INDEX ix_adm_audit_log_retention (RETENTION_UNTIL, IMMUTABLE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 감사 로그';

CREATE TABLE IF NOT EXISTS adm_download_audit_log (
    DOWNLOAD_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '다운로드 감사 로그 순번',
    ADMIN_ID VARCHAR(50) NOT NULL COMMENT '요청 운영자 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 ID',
    SCREEN_ID VARCHAR(100) NULL COMMENT '화면 ID',
    DOWNLOAD_TYPE VARCHAR(50) NOT NULL COMMENT '다운로드 유형',
    TARGET_TYPE VARCHAR(50) NULL COMMENT '대상 유형',
    SEARCH_CONDITION_SUMMARY LONGTEXT NULL COMMENT '검색 조건 요약',
    ROW_COUNT INT NOT NULL DEFAULT 0 COMMENT '다운로드 행 수',
    MASKED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '마스킹 적용 여부',
    INCLUDE_SENSITIVE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감정보 포함 요청 여부',
    REASON VARCHAR(500) NOT NULL COMMENT '다운로드 사유',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    REQUESTED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '요청 일시',
    COMPLETED_AT DATETIME NULL COMMENT '완료 일시',
    STATUS VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '처리 상태',
    FAILURE_REASON VARCHAR(1000) NULL COMMENT '실패 사유',
    FILE_NAME VARCHAR(300) NULL COMMENT '파일명',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (DOWNLOAD_ID),
    INDEX ix_adm_download_audit_log_admin_time (ADMIN_ID, REQUESTED_AT),
    INDEX ix_adm_download_audit_log_type_time (DOWNLOAD_TYPE, REQUESTED_AT),
    INDEX ix_adm_download_audit_log_status_time (STATUS, REQUESTED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 다운로드 감사 로그';

CREATE TABLE IF NOT EXISTS adm_operator_session (
    SESSION_ID VARCHAR(80) NOT NULL COMMENT '세션 ID',
    TOKEN_HASH VARCHAR(512) NOT NULL COMMENT '토큰 해시',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    ROLE_IDS VARCHAR(1000) NULL COMMENT '역할 ID 목록',
    ISSUED_AT DATETIME NOT NULL COMMENT '발급일시',
    EXPIRE_AT DATETIME NOT NULL COMMENT '만료일시',
    REVOKED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '폐기 여부',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (SESSION_ID),
    INDEX ix_adm_operator_session_token (TOKEN_HASH),
    INDEX ix_adm_operator_session_user (OPERATOR_ID, EXPIRE_AT),
    INDEX ix_adm_operator_session_active (REVOKED_YN, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 세션';

CREATE TABLE IF NOT EXISTS adm_dynamic_log_level_rule (
    RULE_ID VARCHAR(80) NOT NULL COMMENT '동적 로그 레벨 규칙 ID',
    TRANSACTION_ID VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL COMMENT '업무 거래 ID',
    MODULE_ID VARCHAR(10) NULL COMMENT '모듈 ID',
    LOG_LEVEL VARCHAR(10) NOT NULL COMMENT '적용 로그 레벨',
    EXPIRE_AT DATETIME NOT NULL COMMENT '만료일시',
    REASON VARCHAR(500) NULL COMMENT '적용 사유',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (RULE_ID),
    INDEX ix_adm_dynamic_log_level_rule_biz_tx (BUSINESS_TRANSACTION_ID, EXPIRE_AT),
    INDEX ix_adm_dynamic_log_level_rule_tx (TRANSACTION_ID, EXPIRE_AT),
    INDEX ix_adm_dynamic_log_level_rule_active (USE_YN, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 동적 로그 레벨 규칙';

CREATE TABLE IF NOT EXISTS adm_ip_allowlist (
    ALLOW_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'IP 허용 목록 순번',
    IP_PATTERN VARCHAR(100) NOT NULL COMMENT '허용 IP 또는 CIDR 패턴',
    DESCRIPTION VARCHAR(500) NULL COMMENT '허용 사유',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ALLOW_ID),
    UNIQUE KEY uk_adm_ip_allowlist_pattern (IP_PATTERN),
    INDEX ix_adm_ip_allowlist_use (USE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 관리자 IP 허용 목록';

CREATE TABLE IF NOT EXISTS adm_mfa_otp_secret (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    SECRET_REF VARCHAR(500) NOT NULL COMMENT 'OTP secret 참조',
    ENABLED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'MFA 사용 여부',
    VERIFIED_AT DATETIME NULL COMMENT 'MFA 검증일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT fk_adm_mfa_otp_secret_operator
        FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator(OPERATOR_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 MFA OTP secret 메타';

CREATE TABLE IF NOT EXISTS adm_password_policy (
    POLICY_ID VARCHAR(50) NOT NULL COMMENT '비밀번호 정책 ID',
    MIN_LENGTH INT NOT NULL DEFAULT 12 COMMENT '최소 길이',
    REQUIRE_UPPER_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '대문자 필수 여부',
    REQUIRE_LOWER_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '소문자 필수 여부',
    REQUIRE_DIGIT_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '숫자 필수 여부',
    REQUIRE_SPECIAL_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '특수문자 필수 여부',
    MAX_FAIL_COUNT INT NOT NULL DEFAULT 5 COMMENT '최대 실패 횟수',
    EXPIRE_DAYS INT NOT NULL DEFAULT 90 COMMENT '만료 일수',
    HISTORY_LIMIT INT NOT NULL DEFAULT 5 COMMENT '재사용 금지 이력 수',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (POLICY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 비밀번호 정책';

CREATE TABLE IF NOT EXISTS adm_password_history (
    HISTORY_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '비밀번호 이력 순번',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    PASSWORD_HASH VARCHAR(512) NOT NULL COMMENT '이전 비밀번호 해시',
    CHANGED_REASON VARCHAR(500) NULL COMMENT '변경 사유',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (HISTORY_ID),
    INDEX ix_adm_password_history_operator_time (OPERATOR_ID, created_at),
    CONSTRAINT fk_adm_password_history_operator
        FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator(OPERATOR_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 비밀번호 변경 이력';

CREATE TABLE IF NOT EXISTS adm_operation_log (
    OPERATION_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '운영 작업 로그 순번',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 ID',
    BUTTON_ID VARCHAR(80) NULL COMMENT '버튼/행위 ID',
    OPERATION_TYPE VARCHAR(50) NOT NULL COMMENT '운영 작업 유형',
    TARGET_TYPE VARCHAR(50) NULL COMMENT '대상 유형',
    TARGET_ID VARCHAR(100) NULL COMMENT '대상 ID',
    RESULT_TYPE CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    RESULT_MESSAGE VARCHAR(1000) NULL COMMENT '결과 메시지',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (OPERATION_ID),
    INDEX ix_adm_operation_log_operator_time (OPERATOR_ID, created_at),
    INDEX ix_adm_operation_log_target_time (TARGET_TYPE, TARGET_ID, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영 작업 로그';
-- ============================================================================
-- specs/sql/40_business_modules_schema.sql
-- ============================================================================
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
-- ============================================================================
-- specs/sql/50_framework_seed_data.sql
-- ============================================================================
-- CPF 프레임워크 초기 코드, 메시지, 응답코드, 설정 데이터입니다.
-- 대상 DB: pfwDB

USE pfwDB;

INSERT INTO pfw_code (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    (NULL, 'CODE_GROUP', 'MODULE', '서비스 모듈 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', '요청 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', '채널 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', '응답 결과 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', '메시지 포맷 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', '동적 로그 레벨 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', '캐시 이름 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'BATCH_JOB_TYPE', '배치 Job 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', '여부 코드 그룹', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_code (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'PFW', '프레임워크 공통 엔진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', '업무 공통 라이브러리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', '관리자 운영 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ACC', '계정 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBR', '회원 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'XYZ', '교육 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', '일반 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', '보상 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', '재시도 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', '웹 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', '모바일 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', '배치 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', '관리자 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', '성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', '오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', '고정 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', '인덱스 파라미터 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'TRACE 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'DEBUG 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'INFO 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'WARN 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'ERROR 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', '전체 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', '코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', '메시지 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', '응답코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', '설정 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'TASKLET', 'Tasklet 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'CHUNK', 'Chunk 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'RETRY', '재처리 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', '예', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', '아니오', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_message (
    message_code, locale, message_format_type, external_message, internal_message,
    parameter_count, parameter_sample, description, created_by, updated_by
) VALUES
    ('MPFW000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'PFW 공통 요청이 정상 처리되었습니다.', 0, NULL, 'PFW 공통 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["memberId","abc"]', 'PFW 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["member"]', 'PFW 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["memberNo"]', 'PFW 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'PFW 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'PFW 인증 필요 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'PFW 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'PFW 업무 규칙 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["mbr"]', 'PFW 외부 연계 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'PFW 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW 헤더 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'PFW @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["MBR01BSE0001"]', 'PFW 메타데이터 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'PFW 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["mbr"]', 'PFW endpoint 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900004', 'ko', 'INDEXED', '동적 로그레벨 요청이 올바르지 않습니다.', 'PFW 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'PFW 동적 로그 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'PFW 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'PFW 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'PFW 데이터베이스 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC000000', 'ko', 'FIXED', '성공', 'ACC 요청이 정상 처리되었습니다.', 0, NULL, 'ACC 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC010001', 'ko', 'INDEXED', '계정 요청 값이 올바르지 않습니다.', 'ACC 파라미터 검증에 실패했습니다. field={0}', 1, '["accountId"]', 'ACC 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC010002', 'ko', 'INDEXED', '계정 정보를 찾을 수 없습니다.', 'ACC 조회 대상이 없습니다. target={0}', 1, '["account"]', 'ACC 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR000000', 'ko', 'FIXED', '성공', 'MBR 요청이 정상 처리되었습니다.', 0, NULL, 'MBR 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010001', 'ko', 'FIXED', '회원이 생성되었습니다.', 'MBR 회원 데이터가 생성되었습니다.', 0, NULL, 'MBR 생성 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010002', 'ko', 'FIXED', '회원이 수정되었습니다.', 'MBR 회원 데이터가 수정되었습니다.', 0, NULL, 'MBR 수정 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010003', 'ko', 'FIXED', '회원이 삭제되었습니다.', 'MBR 회원 데이터가 삭제되었습니다.', 0, NULL, 'MBR 삭제 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010101', 'ko', 'FIXED', '회원 요청 형식이 올바르지 않습니다.', 'MBR 요청 형식이 올바르지 않습니다.', 0, NULL, 'MBR bad request 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010102', 'ko', 'INDEXED', '유효하지 않은 회원 파라미터입니다.', 'MBR 파라미터 검증에 실패했습니다. field={0}', 1, '["memberId"]', 'MBR 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010103', 'ko', 'INDEXED', '회원 정보를 찾을 수 없습니다.', 'MBR 조회 대상이 없습니다. target={0}', 1, '["member"]', 'MBR 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010104', 'ko', 'INDEXED', '중복된 회원 데이터가 있습니다.', 'MBR 중복 데이터가 감지되었습니다. key={0}', 1, '["memberNo"]', 'MBR 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010105', 'ko', 'INDEXED', '회원 입력값 검증에 실패했습니다.', 'MBR 입력값 검증에 실패했습니다. field={0}', 1, '["name"]', 'MBR 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR990000', 'ko', 'INDEXED', '회원 처리 중 오류가 발생했습니다.', 'MBR 내부 서버 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'MBR 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MXYZ090001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=XYZ_EDU_SAMPLE', 2, '["회원번호","M0001"]', 'XYZ 동적 중복 교육 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF 교육 시스템에 오신 것을 환영합니다.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF education system.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_format_type = VALUES(message_format_type),
    external_message = VALUES(external_message),
    internal_message = VALUES(internal_message),
    parameter_count = VALUES(parameter_count),
    parameter_sample = VALUES(parameter_sample),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_response_code (
    response_code, message_code, result_type, module_id, response_group, sequence_no,
    http_status, description, created_by, updated_by
) VALUES
    ('SPFW000000', 'MPFW000000', 'S', 'PFW', '00', '0000', 200, 'PFW 공통 성공', 'SYSTEM', 'SYSTEM'),
    ('EPFW010001', 'MPFW010001', 'E', 'PFW', '01', '0001', 400, '파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010002', 'MPFW010002', 'E', 'PFW', '01', '0002', 404, '미존재 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010003', 'MPFW010003', 'E', 'PFW', '01', '0003', 409, '중복 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010004', 'MPFW010004', 'E', 'PFW', '01', '0004', 400, '검증 실패', 'SYSTEM', 'SYSTEM'),
    ('EPFW010005', 'MPFW010005', 'E', 'PFW', '01', '0005', 401, '인증 필요', 'SYSTEM', 'SYSTEM'),
    ('EPFW010006', 'MPFW010006', 'E', 'PFW', '01', '0006', 403, '권한 없음', 'SYSTEM', 'SYSTEM'),
    ('EPFW020001', 'MPFW020001', 'E', 'PFW', '02', '0001', 400, '업무 규칙 위반', 'SYSTEM', 'SYSTEM'),
    ('EPFW030001', 'MPFW030001', 'E', 'PFW', '03', '0001', 502, '외부 연계 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW900001', 'MPFW900001', 'E', 'PFW', '90', '0001', 400, '필수 거래 헤더 누락', 'SYSTEM', 'SYSTEM'),
    ('EPFW900002', 'MPFW900002', 'E', 'PFW', '90', '0002', 500, '거래 메타데이터 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW900003', 'MPFW900003', 'E', 'PFW', '90', '0003', 500, '서비스 endpoint 미등록', 'SYSTEM', 'SYSTEM'),
    ('EPFW900004', 'MPFW900004', 'E', 'PFW', '90', '0004', 400, '동적 로그 규칙 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW990000', 'MPFW990000', 'E', 'PFW', '99', '0000', 500, '내부 서버 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW990001', 'MPFW990001', 'E', 'PFW', '99', '0001', 500, '데이터베이스 오류', 'SYSTEM', 'SYSTEM'),
    ('SACC000000', 'MACC000000', 'S', 'ACC', '00', '0000', 200, 'ACC 성공', 'SYSTEM', 'SYSTEM'),
    ('EACC010001', 'MACC010001', 'E', 'ACC', '01', '0001', 400, 'ACC 파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EACC010002', 'MACC010002', 'E', 'ACC', '01', '0002', 404, 'ACC 미존재', 'SYSTEM', 'SYSTEM'),
    ('SMBR000000', 'MMBR000000', 'S', 'MBR', '00', '0000', 200, 'MBR 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010001', 'MMBR010001', 'S', 'MBR', '01', '0001', 200, 'MBR 생성 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010002', 'MMBR010002', 'S', 'MBR', '01', '0002', 200, 'MBR 수정 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010003', 'MMBR010003', 'S', 'MBR', '01', '0003', 200, 'MBR 삭제 성공', 'SYSTEM', 'SYSTEM'),
    ('EMBR010001', 'MMBR010101', 'E', 'MBR', '01', '0001', 400, 'MBR 요청 형식 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBR010002', 'MMBR010102', 'E', 'MBR', '01', '0002', 400, 'MBR 파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBR010003', 'MMBR010103', 'E', 'MBR', '01', '0003', 404, 'MBR 미존재', 'SYSTEM', 'SYSTEM'),
    ('EMBR010004', 'MMBR010104', 'E', 'MBR', '01', '0004', 409, 'MBR 중복', 'SYSTEM', 'SYSTEM'),
    ('EMBR010005', 'MMBR010105', 'E', 'MBR', '01', '0005', 400, 'MBR 검증 실패', 'SYSTEM', 'SYSTEM'),
    ('EMBR990000', 'MMBR990000', 'E', 'MBR', '99', '0000', 500, 'MBR 내부 오류', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_code = VALUES(message_code),
    result_type = VALUES(result_type),
    module_id = VALUES(module_id),
    response_group = VALUES(response_group),
    sequence_no = VALUES(sequence_no),
    http_status = VALUES(http_status),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_config (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES
    ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'CMN 캐시 기동 시 선적재 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', '캐시 선적재 실패 시 기동 실패 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', '캐시 갱신 이벤트 polling 주기', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', '기본 CMN 메시지 브로커 유형', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'PFW HTTP client 연결 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'PFW HTTP client 읽기 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.SESSION_TTL_SECONDS', '3600', 'NUMBER', 'ADM 세션 TTL 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'ADM 비밀번호 만료 일수', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MIN_LENGTH', '10', 'NUMBER', 'ADM 비밀번호 최소 길이', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MAX_FAIL_COUNT', '5', 'NUMBER', 'ADM 로그인 실패 잠금 기준', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.BATCH.DEFAULT_LOCK_SECONDS', '3600', 'NUMBER', '배치 기본 lock 만료 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.FEATURE.SAMPLE_ENABLED', 'Y', 'BOOLEAN', '샘플 API와 교육 flow 활성화 여부', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    config_type = VALUES(config_type),
    description = VALUES(description),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_log_policy (
    policy_key, policy_name, target_type, target_id, log_level,
    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
    error_stack_log_yn, retention_days, sampling_rate, priority, active_yn,
    description, created_by, updated_by
) VALUES
    ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    policy_name = VALUES(policy_name),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    log_level = VALUES(log_level),
    db_log_enabled_yn = VALUES(db_log_enabled_yn),
    file_log_enabled_yn = VALUES(file_log_enabled_yn),
    request_body_log_yn = VALUES(request_body_log_yn),
    response_body_log_yn = VALUES(response_body_log_yn),
    error_stack_log_yn = VALUES(error_stack_log_yn),
    retention_days = VALUES(retention_days),
    sampling_rate = VALUES(sampling_rate),
    priority = VALUES(priority),
    active_yn = VALUES(active_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_security_jwt_key (
    KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by
) VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    ISSUER = VALUES(ISSUER),
    ALGORITHM = VALUES(ALGORITHM),
    SECRET_REF = VALUES(SECRET_REF),
    ACTIVE_YN = VALUES(ACTIVE_YN),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_cache_refresh_event (
    cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by
)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

INSERT INTO BATCH_JOB_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_SEQ);

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_EXECUTION_SEQ);

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_STEP_EXECUTION_SEQ);

INSERT INTO pfw_batch_instance (
    instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    '로컬 배치 인스턴스',
    'localhost',
    8099,
    'Y',
    NOW(3),
    'XYZ EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    instance_name = VALUES(instance_name),
    host_name = VALUES(host_name),
    server_port = VALUES(server_port),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_batch_worker (
    worker_id, server_instance_id, host_name, process_id, thread_name, worker_status,
    active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    'local-batch-01',
    'localhost',
    'seed',
    'seed-main',
    'IDLE',
    'Y',
    NOW(3),
    NULL,
    NULL,
    '로컬 smoke 검증용 배치 worker heartbeat',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    server_instance_id = VALUES(server_instance_id),
    host_name = VALUES(host_name),
    process_id = VALUES(process_id),
    thread_name = VALUES(thread_name),
    worker_status = VALUES(worker_status),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    current_job_id = VALUES(current_job_id),
    current_execution_id = VALUES(current_execution_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_batch_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_TASKLET_JOB', 'CPF 교육 Tasklet Job', 'TASKLET', '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_JOB', 'CPF 교육 Chunk Job', 'CHUNK', '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_RETRY_JOB', 'CPF 교육 재처리 Job', 'RETRY', '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_batch_schedule (
    schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn,
    holiday_policy, available_start_time, available_end_time, run_date_pattern,
    timezone, enabled_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_TASKLET_DAILY', 'CPF_EDU_TASKLET_JOB', '0 0 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:00:00', '04:00:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_DAILY', 'CPF_EDU_CHUNK_JOB', '0 30 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:30:00', '05:30:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    job_id = VALUES(job_id),
    cron_expression = VALUES(cron_expression),
    calendar_id = VALUES(calendar_id),
    business_day_only_yn = VALUES(business_day_only_yn),
    holiday_policy = VALUES(holiday_policy),
    available_start_time = VALUES(available_start_time),
    available_end_time = VALUES(available_end_time),
    run_date_pattern = VALUES(run_date_pattern),
    timezone = VALUES(timezone),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_batch_job_relation (
    job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_CHUNK_JOB', 'CPF_EDU_TASKLET_JOB', 'PREDECESSOR', 'COMPLETED', 'COMPLETED', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_TASKLET_JOB', 'CPF_EDU_CHUNK_JOB', 'TRIGGER', 'COMPLETED', 'COMPLETED', 20, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    trigger_condition = VALUES(trigger_condition),
    required_status = VALUES(required_status),
    sort_order = VALUES(sort_order),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_batch_execution (
    job_id, schedule_id, job_parameters, execution_status, batch_instance_id, server_instance_id,
    worker_id, transaction_global_id, start_time, end_time,
    read_count, write_count, skip_count, requested_by, created_by, updated_by
)
SELECT
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000XYZlocal010000001',
    DATE_SUB(NOW(3), INTERVAL 10 MINUTE),
    DATE_SUB(NOW(3), INTERVAL 9 MINUTE),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_batch_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);

SET @cpf_edu_execution_id = (
    SELECT execution_id
    FROM pfw_batch_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
);

INSERT INTO pfw_batch_step_execution (
    execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status,
    start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by
)
SELECT @cpf_edu_execution_id, NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', DATE_SUB(NOW(3), INTERVAL 10 MINUTE), DATE_SUB(NOW(3), INTERVAL 9 MINUTE), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_batch_step_execution
      WHERE execution_id = @cpf_edu_execution_id
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO pfw_batch_execution_target (
    execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at,
    dispatch_status, dispatch_reason, created_by, updated_by
)
SELECT
    @cpf_edu_execution_id,
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST(CONCAT(CURRENT_DATE, ' 02:00:00') AS DATETIME),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_batch_execution_target
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

INSERT INTO pfw_business_day_calendar (
    calendar_id, business_date, holiday_yn, business_day_yn, description, created_by, updated_by
) VALUES
    ('DEFAULT', CURRENT_DATE, 'N', 'Y', '로컬 smoke 검증용 기본 영업일', 'SYSTEM', 'SYSTEM'),
    ('DEFAULT', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'N', 'Y', '로컬 smoke 검증용 다음 영업일', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    holiday_yn = VALUES(holiday_yn),
    business_day_yn = VALUES(business_day_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_notification_rule (
    event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by
) VALUES
    ('BATCH_EXECUTION', 'FAILED', 'ADM', 'BATCH_FAILED_DEFAULT', 'ERROR', 'ADM_BATCH_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_EVENT', 'LOGIN_FAILURE', 'ADM', 'SECURITY_LOGIN_FAILURE', 'WARN', 'ADM_SECURITY_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    template_code = VALUES(template_code),
    severity = VALUES(severity),
    receiver_group = VALUES(receiver_group),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_notification_delivery_log (
    rule_id, event_type, target_type, target_id, receiver, delivery_status, delivery_message, created_by, updated_by
)
SELECT
    rule_id,
    'BATCH_EXECUTION',
    'pfw_batch_execution',
    CAST(@cpf_edu_execution_id AS CHAR),
    'ADM_BATCH_OPERATOR',
    'SKIPPED',
    '로컬 seed 알림 발송 로그 샘플입니다.',
    'SYSTEM',
    'SYSTEM'
FROM pfw_notification_rule
WHERE event_type = 'BATCH_EXECUTION'
  AND event_sub_type = 'FAILED'
  AND @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_notification_delivery_log
      WHERE event_type = 'BATCH_EXECUTION'
        AND target_id = CAST(@cpf_edu_execution_id AS CHAR)
        AND receiver = 'ADM_BATCH_OPERATOR'
  )
LIMIT 1;

INSERT INTO pfw_batch_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'TASKLET',
    'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, updated_by
) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'batCenterCutSampleTargetProvider',
    'batCenterCutSampleHandler',
    10,
    3,
    'Y',
    'PFW 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    batch_job_id = VALUES(batch_job_id),
    center_cut_job_name = VALUES(center_cut_job_name),
    provider_key = VALUES(provider_key),
    handler_key = VALUES(handler_key),
    chunk_size = VALUES(chunk_size),
    retry_limit = VALUES(retry_limit),
    use_yn = VALUES(use_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_center_cut_parameter (
    center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_BAT_CENTER_CUT_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_BAT_CENTER_CUT_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
-- ============================================================================
-- specs/sql/55_cmn_seed_data.sql
-- ============================================================================
-- CMN 업무 공통 기능 초기 데이터입니다.
-- 교육 샘플에서 바로 호출 가능한 채번 기준과 예시 로그를 등록합니다.

USE cmnDB;

INSERT INTO cmn_sequence (
    sequence_key,
    business_area,
    business_key,
    sequence_kind,
    channel_code,
    prefix,
    date_pattern,
    current_value,
    start_value,
    increment_by,
    min_value,
    max_value,
    range_size,
    number_length,
    reset_cycle,
    reset_pattern,
    reset_timezone,
    last_reset_key,
    log_enabled_yn,
    retention_days,
    description,
    use_yn,
    created_by,
    updated_by
) VALUES (
    'CMN_EDU_ORDER',
    'CMN_EDU',
    'ORDER',
    'ORDER_NO',
    'WEB',
    'EDU',
    'yyyyMMdd',
    0,
    1,
    1,
    1,
    999999,
    1,
    6,
    'DAY',
    'yyyyMMdd',
    'Asia/Seoul',
    DATE_FORMAT(CURRENT_DATE, '%Y%m%d'),
    'Y',
    365,
    'CMN 교육용 주문번호 채번 샘플',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    business_area = VALUES(business_area),
    business_key = VALUES(business_key),
    sequence_kind = VALUES(sequence_kind),
    channel_code = VALUES(channel_code),
    prefix = VALUES(prefix),
    date_pattern = VALUES(date_pattern),
    start_value = VALUES(start_value),
    increment_by = VALUES(increment_by),
    min_value = VALUES(min_value),
    max_value = VALUES(max_value),
    range_size = VALUES(range_size),
    number_length = VALUES(number_length),
    reset_cycle = VALUES(reset_cycle),
    reset_pattern = VALUES(reset_pattern),
    reset_timezone = VALUES(reset_timezone),
    log_enabled_yn = VALUES(log_enabled_yn),
    retention_days = VALUES(retention_days),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cmn_notification_log (
    notification_type,
    receiver,
    title,
    message,
    send_status,
    send_result,
    transaction_id,
    trace_id,
    created_by,
    updated_by
)
SELECT
    'EMAIL',
    'developer@example.com',
    'CMN 알림 로그 샘플',
    'CMN 공통 알림 로그 테이블 연동을 확인하기 위한 초기 데이터입니다.',
    'READY',
    NULL,
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cmn_notification_log
    WHERE transaction_id = 'INITIAL_SQL_SEED'
      AND title = 'CMN 알림 로그 샘플'
);

INSERT INTO cmn_business_log (
    business_area,
    business_key,
    log_type,
    log_message,
    log_payload,
    transaction_id,
    trace_id,
    created_by,
    updated_by
)
SELECT
    'CMN_EDU',
    'INITIAL',
    'SEED',
    'CMN 공통 업무 로그 테이블 연동을 확인하기 위한 초기 데이터입니다.',
    '{"source":"55_cmn_seed_data.sql"}',
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cmn_business_log
    WHERE business_area = 'CMN_EDU'
      AND business_key = 'INITIAL'
      AND log_type = 'SEED'
);
-- ============================================================================
-- specs/sql/60_adm_seed_data.sql
-- ============================================================================
-- ADM 초기 역할, 메뉴, 버튼 권한, 보안 정책, 로컬 계정 데이터입니다.
-- 대상 DB: admDB

USE admDB;

INSERT INTO adm_role (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES
    ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    ROLE_NAME = VALUES(ROLE_NAME),
    ROLE_TYPE = VALUES(ROLE_TYPE),
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_menu (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('DASHBOARD', NULL, '대시보드', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, '온라인 거래 로그', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, '감사 로그', '/adm#audit-logs', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER', NULL, '회원 관리', '/adm#members', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH', NULL, '배치 관제', '/adm#batch', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', NULL, '알림 관리', '/adm#notifications', 55, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', NULL, '다운로드 감사', '/adm#downloads', 58, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, '캐시 관리', '/adm#cache', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE', NULL, '메시지 관리', '/adm#messages', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE', NULL, '코드 관리', '/adm#codes', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, '응답코드 관리', '/adm#response-codes', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG', NULL, '설정 관리', '/adm#configs', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, '동적 로그 레벨', '/adm#log-level', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD', NULL, '비밀번호 관리', '/adm#password', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY', NULL, '보안 운영', '/adm#security', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', NULL, '권한 관리', '/adm#permissions', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, '운영자 관리', '/adm#operators', 150, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    PARENT_MENU_ID = VALUES(PARENT_MENU_ID),
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_button (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('LOG_LIST_READ', 'LOG_LIST', 'READ', '조회', 'GET', '/adm/api/logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DETAIL', 'LOG_LIST', 'DETAIL', '상세 조회', 'GET', '/adm/api/logs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DOWNLOAD', 'LOG_LIST', 'DOWNLOAD', '다운로드', 'GET', '/adm/api/logs/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_SCAN', 'TRANSACTION_META', 'SCAN', '거래 메타 스캔', 'POST', '/adm/api/transactions/scan', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG_READ', 'AUDIT_LOG', 'READ', '조회', 'GET', '/adm/api/audit-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_READ', 'MEMBER', 'READ', '회원 조회', 'GET', '/adm/api/members/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_CREATE', 'MEMBER', 'CREATE', '회원 등록', 'POST', '/adm/api/members', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_UPDATE', 'MEMBER', 'UPDATE', '회원 수정', 'PUT', '/adm/api/members/*', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_STATUS', 'MEMBER', 'STATUS', '회원 상태 변경', 'PUT', '/adm/api/members/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_GRANT', 'MEMBER', 'ROLE_GRANT', '회원 권한 부여', 'POST', '/adm/api/members/*/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_REVOKE', 'MEMBER', 'ROLE_REVOKE', '회원 권한 회수', 'DELETE', '/adm/api/members/*/roles/*', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_READ', 'BATCH', 'READ', '조회', 'GET', '/adm/api/batch/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_REGISTER', 'BATCH', 'REGISTER', '배치 등록', 'POST', '/adm/api/batch/jobs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_EXECUTE', 'BATCH', 'EXECUTE', '수동 실행', 'POST', '/adm/api/batch/*/run', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RETRY', 'BATCH', 'RETRY', '실패 재수행', 'POST', '/adm/api/batch/executions/*/retry', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STOP', 'BATCH', 'STOP', '실행 중지', 'POST', '/adm/api/batch/executions/*/stop', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULE', 'BATCH', 'SCHEDULE', '스케줄 변경', 'POST', '/adm/api/batch/schedules/**', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_CALENDAR_SAVE', 'BATCH', 'CALENDAR_SAVE', '영업일 저장', 'POST', '/adm/api/batch/calendar', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SIMULATION', 'BATCH', 'SIMULATION', '수행 시뮬레이션', 'GET', '/adm/api/batch/schedules/*/simulation', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RELATION_READ', 'BATCH', 'RELATION_READ', '배치 관계 조회', 'GET', '/adm/api/batch/relations', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_TARGET_READ', 'BATCH', 'TARGET_READ', '수행 대상 조회', 'GET', '/adm/api/batch/execution-targets', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULER_RUN', 'BATCH', 'SCHEDULER_RUN', '스케줄러 1회 실행', 'POST', '/adm/api/batch/scheduler/run-once', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_JOB_DETAIL', 'BATCH', 'DETAIL', 'Job 상세 조회', 'GET', '/adm/api/batch/jobs/*', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STEP_READ', 'BATCH', 'STEP_READ', 'Step 이력 조회', 'GET', '/adm/api/batch/steps', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_WORKER_READ', 'BATCH', 'WORKER_READ', 'Worker 상태 조회', 'GET', '/adm/api/batch/workers', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_READ', 'BATCH', 'LOCK_READ', 'Lock 조회', 'GET', '/adm/api/batch/locks', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_RELEASE', 'BATCH', 'LOCK_RELEASE', 'Lock 강제 해제', 'POST', '/adm/api/batch/locks/release', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_READ', 'BATCH', 'GHOST_READ', 'Ghost 후보 조회', 'GET', '/adm/api/batch/ghost-candidates', 170, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_ACTION', 'BATCH', 'GHOST_ACTION', 'Ghost 조치', 'POST', '/adm/api/batch/ghost-candidates/*/actions', 180, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_OPERATION_READ', 'BATCH', 'OPERATION_READ', '운영 작업 로그 조회', 'GET', '/adm/api/batch/operations', 190, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_READ', 'NOTIFICATION', 'READ', '알림 조회', 'GET', '/adm/api/notifications/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_WRITE', 'NOTIFICATION', 'WRITE', '알림 등록/수정', 'POST', '/adm/api/notifications/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_DISABLE', 'NOTIFICATION', 'DISABLE', '알림 비활성화', 'PUT', '/adm/api/notifications/rules/*/disable', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_TEST_SEND', 'NOTIFICATION', 'TEST_SEND', '알림 테스트 발송', 'POST', '/adm/api/notifications/rules/*/test-send', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_READ', 'DOWNLOAD', 'READ', '다운로드 감사 조회', 'GET', '/adm/api/downloads/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_EXECUTE', 'DOWNLOAD', 'DOWNLOAD', 'CSV 다운로드', 'POST', '/adm/api/downloads/csv', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_READ', 'CACHE', 'READ', '조회', 'GET', '/adm/api/cache/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_REFRESH', 'CACHE', 'REFRESH', '캐시 갱신', 'POST', '/adm/api/cache/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_READ', 'MESSAGE', 'READ', '조회', 'GET', '/adm/api/messages/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_WRITE', 'MESSAGE', 'WRITE', '등록/수정', 'POST', '/adm/api/messages/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_DISABLE', 'MESSAGE', 'DISABLE', '비활성', 'DELETE', '/adm/api/messages/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_READ', 'CODE', 'READ', '조회', 'GET', '/adm/api/codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_WRITE', 'CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_DISABLE', 'CODE', 'DISABLE', '비활성', 'DELETE', '/adm/api/codes/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_READ', 'RESPONSE_CODE', 'READ', '조회', 'GET', '/adm/api/response-codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_WRITE', 'RESPONSE_CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/response-codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_READ', 'CONFIG', 'READ', '조회', 'GET', '/adm/api/configs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_WRITE', 'CONFIG', 'WRITE', '수정', 'POST', '/adm/api/configs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_READ', 'DYNAMIC_LOG', 'READ', '조회', 'GET', '/adm/api/log-level/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_WRITE', 'DYNAMIC_LOG', 'WRITE', '적용', 'POST', '/adm/api/log-level/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_READ', 'LOG_POLICY', 'READ', '조회', 'GET', '/adm/api/log-policies/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_WRITE', 'LOG_POLICY', 'WRITE', '등록/수정', 'POST', '/adm/api/log-policies/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_OVERRIDE', 'LOG_POLICY', 'OVERRIDE', '임시 override', 'POST', '/adm/api/log-policies/overrides', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_REFRESH', 'LOG_POLICY', 'CACHE_REFRESH', '정책 캐시 새로고침', 'POST', '/adm/api/log-policies/cache/refresh', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_CLEAR', 'LOG_POLICY', 'CACHE_CLEAR', '정책 캐시 전체 삭제', 'POST', '/adm/api/log-policies/cache/clear', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_READ', 'PASSWORD', 'READ', '정책 조회', 'GET', '/adm/api/operators/password-policy/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_RESET', 'PASSWORD', 'RESET_PASSWORD', '비밀번호 초기화', 'POST', '/adm/api/operators/*/password/reset', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_UNLOCK', 'PASSWORD', 'UNLOCK', '잠금 해제', 'POST', '/adm/api/operators/*/unlock', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_SESSION_REVOKE', 'PASSWORD', 'REVOKE_SESSION', '세션 강제 종료', 'POST', '/adm/api/operators/sessions/*/revoke', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_READ', 'SECURITY', 'READ', '조회', 'GET', '/adm/api/security/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_WRITE', 'SECURITY', 'WRITE', '보안 설정 변경', 'POST', '/adm/api/security/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_READ', 'PERMISSION', 'READ', '조회', 'GET', '/adm/api/permissions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_WRITE', 'PERMISSION', 'WRITE', '권한 변경', 'POST', '/adm/api/permissions/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_READ', 'OPERATOR', 'READ', '조회', 'GET', '/adm/api/operators/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_CREATE', 'OPERATOR', 'CREATE', '운영자 등록', 'POST', '/adm/api/operators', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    MENU_ID = VALUES(MENU_ID),
    ACTION_CODE = VALUES(ACTION_CODE),
    BUTTON_NAME = VALUES(BUTTON_NAME),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATTERN = VALUES(API_PATTERN),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_password_policy (
    POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN,
    REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by
) VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MIN_LENGTH = VALUES(MIN_LENGTH),
    REQUIRE_UPPER_YN = VALUES(REQUIRE_UPPER_YN),
    REQUIRE_LOWER_YN = VALUES(REQUIRE_LOWER_YN),
    REQUIRE_DIGIT_YN = VALUES(REQUIRE_DIGIT_YN),
    REQUIRE_SPECIAL_YN = VALUES(REQUIRE_SPECIAL_YN),
    MAX_FAIL_COUNT = VALUES(MAX_FAIL_COUNT),
    EXPIRE_DAYS = VALUES(EXPIRE_DAYS),
    HISTORY_LIMIT = VALUES(HISTORY_LIMIT),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_operator (
    OPERATOR_ID,
    OPERATOR_NAME,
    PASSWORD_HASH,
    LOCKED_YN,
    FAIL_COUNT,
    PASSWORD_CHANGED_AT,
    PASSWORD_EXPIRE_AT,
    PASSWORD_CHANGE_REQUIRED_YN,
    USE_YN,
    created_by,
    updated_by
) VALUES (
    'admin',
    '로컬 관리자',
    'PBKDF2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$cjgjgGQwgcZ0+fFaA8Z4qBJkZfszRZ73BSBIMXAJkqI=',
    'N',
    0,
    DATE_SUB(NOW(), INTERVAL 91 DAY),
    DATE_ADD(NOW(), INTERVAL 90 DAY),
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    OPERATOR_NAME = VALUES(OPERATOR_NAME),
    PASSWORD_HASH = VALUES(PASSWORD_HASH),
    LOCKED_YN = VALUES(LOCKED_YN),
    FAIL_COUNT = VALUES(FAIL_COUNT),
    PASSWORD_EXPIRE_AT = VALUES(PASSWORD_EXPIRE_AT),
    PASSWORD_CHANGE_REQUIRED_YN = VALUES(PASSWORD_CHANGE_REQUIRED_YN),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_operator_role (OPERATOR_ID, ROLE_ID, created_by, updated_by)
VALUES ('admin', 'ADM_ADMIN', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_menu
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'BATCH', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('MEMBER', 'BATCH', 'DOWNLOAD', 'CACHE') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID = 'MEMBER' THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_menu
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_UPDATE', 'MEMBER_STATUS', 'MEMBER_ROLE_GRANT', 'MEMBER_ROLE_REVOKE', 'BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

-- ADM API 권한은 버튼/행위 권한을 실제 Controller path와 연결하기 위한 서버 권한검사 메타입니다.
INSERT INTO adm_api_permission (
    API_PERMISSION_ID,
    API_GROUP_CODE,
    HTTP_METHOD,
    API_PATH,
    API_NAME,
    PERMISSION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    created_by,
    updated_by
)
SELECT
    CONCAT('API_', BUTTON_ID),
    MENU_ID,
    COALESCE(HTTP_METHOD, 'ANY'),
    API_PATTERN,
    BUTTON_NAME,
    ACTION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    'SYSTEM',
    'SYSTEM'
FROM adm_button
WHERE API_PATTERN IS NOT NULL
ON DUPLICATE KEY UPDATE
    API_GROUP_CODE = VALUES(API_GROUP_CODE),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATH = VALUES(API_PATH),
    API_NAME = VALUES(API_NAME),
    PERMISSION_CODE = VALUES(PERMISSION_CODE),
    MENU_ID = VALUES(MENU_ID),
    BUTTON_ID = VALUES(BUTTON_ID),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (
    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
    MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by
) VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    API_GROUP_CODE = VALUES(API_GROUP_CODE),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATH = VALUES(API_PATH),
    API_NAME = VALUES(API_NAME),
    PERMISSION_CODE = VALUES(PERMISSION_CODE),
    MENU_ID = VALUES(MENU_ID),
    BUTTON_ID = VALUES(BUTTON_ID),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID, ap.API_PERMISSION_ID, rb.ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button rb
JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_ip_allowlist (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('127.0.0.1', '로컬 개발 PC', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_audit_log (
    TRANSACTION_ID,
    TRACE_ID,
    OPERATOR_ID,
    MENU_ID,
    ACTION_TYPE,
    TARGET_TYPE,
    TARGET_ID,
    REASON,
    REQUEST_BODY,
    CLIENT_IP,
    created_by,
    updated_by
) SELECT
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'admin',
    'DASHBOARD',
    'SEED',
    'ADM',
    'INITIAL_DATA',
    'ADM 초기 데이터 등록',
    NULL,
    '127.0.0.1',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM adm_audit_log
    WHERE TRANSACTION_ID = 'INITIAL_SQL_SEED'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);
-- ============================================================================
-- specs/sql/70_test_data.sql
-- ============================================================================
-- 로컬 및 통합 검증용 테스트 데이터입니다.

USE pfwDB;

INSERT INTO pfw_file_exchange_log (
    EXCHANGE_ID,
    TRANSACTION_ID,
    TRACE_ID,
    BUSINESS_TRANSACTION_ID,
    ACTION_TYPE,
    PROTOCOL,
    DIRECTION,
    EXECUTED_YN,
    SUCCESS_YN,
    HOST,
    SOURCE_PATH,
    TARGET_PATH,
    REQUEST_USER,
    MESSAGE,
    created_by,
    updated_by
) VALUES (
    'FILE-LOCAL-SAMPLE-001',
    'TEST_TRANSACTION',
    'TEST_TRACE',
    'XYZ08EDU0001',
    'LOCAL_WRITE',
    'LOCAL',
    'WRITE',
    'Y',
    'Y',
    'localhost',
    '/tmp/cpf/source.txt',
    '/tmp/cpf/target.txt',
    'SYSTEM',
    '로컬 파일 교환 샘플 이력입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MESSAGE = VALUES(MESSAGE),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE cmnDB;

INSERT INTO cmn_edu_query_item (
    item_id, item_name, category_code, status_code, owner_member_no, use_yn, created_by, updated_by
) VALUES
    (1, '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'M000000001', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'M000000002', 'Y', 'SYSTEM', 'SYSTEM'),
    (3, 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000003', 'Y', 'SYSTEM', 'SYSTEM'),
    (4, 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000004', 'Y', 'SYSTEM', 'SYSTEM'),
    (5, '검색 조건 정규화', 'QUERY', 'INACTIVE', 'M000000005', 'Y', 'SYSTEM', 'SYSTEM'),
    (6, '정렬 whitelist', 'QUERY', 'ACTIVE', 'M000000006', 'Y', 'SYSTEM', 'SYSTEM'),
    (7, '하위 호출 헤더 전파', 'HEADER', 'ACTIVE', 'M000000007', 'Y', 'SYSTEM', 'SYSTEM'),
    (8, 'Swagger 조회 예시', 'DOC', 'ACTIVE', 'M000000008', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    owner_member_no = VALUES(owner_member_no),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE accDB;

INSERT INTO acc_account (account_id, account_no, account_name, account_status, balance, description, created_by, updated_by)
VALUES
    (1, '100-000-000001', 'ACC 샘플 계정 1', 'ACTIVE', 100000.00, 'ACC 계정 샘플 1', 'SYSTEM', 'SYSTEM'),
    (2, '100-000-000002', 'ACC 샘플 계정 2', 'ACTIVE', 250000.00, 'ACC 계정 샘플 2', 'SYSTEM', 'SYSTEM'),
    (3, '100-000-000003', 'ACC 휴면 계정', 'DORMANT', 0.00, 'ACC 휴면 계정 샘플', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    account_no = VALUES(account_no),
    account_name = VALUES(account_name),
    account_status = VALUES(account_status),
    balance = VALUES(balance),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE mbrDB;

INSERT INTO mbr_member (
    id, member_no, customer_no, login_id, name, email, mobile_no,
    password_hash, login_fail_count, password_change_required_yn, password_expire_at,
    member_status, lock_yn, withdraw_yn, channel_code, description, created_by, updated_by
) VALUES
    (1, 'M000000001', 'C000000001', 'mbr001', '회원 1', 'mbr001@example.com', '010-1000-0001', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'WEB', 'MBR 샘플 회원 1', 'SYSTEM', 'SYSTEM'),
    (2, 'M000000002', 'C000000002', 'mbr002', '회원 2', 'mbr002@example.com', '010-1000-0002', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'MOBILE', 'MBR 샘플 회원 2', 'SYSTEM', 'SYSTEM'),
    (3, 'M000000003', 'C000000003', 'mbr003', '회원 3', 'mbr003@example.com', '010-1000-0003', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'DORMANT', 'N', 'N', 'WEB', 'MBR 휴면 회원 샘플', 'SYSTEM', 'SYSTEM'),
    (100, 'M000000100', 'C000000100', 'search.target', '검색 대상', 'search@example.com', '010-9999-0100', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'WEB', 'MBR 이름 검색 테스트 행', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    customer_no = VALUES(customer_no),
    login_id = VALUES(login_id),
    password_hash = VALUES(password_hash),
    login_fail_count = VALUES(login_fail_count),
    password_change_required_yn = VALUES(password_change_required_yn),
    password_expire_at = VALUES(password_expire_at),
    name = VALUES(name),
    email = VALUES(email),
    mobile_no = VALUES(mobile_no),
    member_status = VALUES(member_status),
    lock_yn = VALUES(lock_yn),
    withdraw_yn = VALUES(withdraw_yn),
    channel_code = VALUES(channel_code),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_role (
    member_id, service_code, role_code, role_name, grade_code, temporary_yn, expire_at,
    granted_by, use_yn, created_by, updated_by
) VALUES
    (1, 'MBR', 'MBR_USER', '일반 회원', 'NORMAL', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, 'MBR', 'MBR_PREMIUM', '프리미엄 회원', 'PREMIUM', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    grade_code = VALUES(grade_code),
    temporary_yn = VALUES(temporary_yn),
    expire_at = VALUES(expire_at),
    granted_by = VALUES(granted_by),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_login_history (
    member_id, login_domain, member_no, customer_no, login_id, login_result, login_ip, user_agent, failure_reason,
    transaction_global_id, module_id, was_id, server_instance_id, created_by, updated_by
)
SELECT 1, 'MBR', 'M000000001', 'C000000001', 'mbr001', 'SUCCESS', '127.0.0.1', 'SQL-SEED', NULL,
       '20260615120000000MBRlocal010000001', 'MBR', 'local01', 'local-mbr:seed', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM mbr_member_login_history
    WHERE member_id = 1
      AND login_id = 'mbr001'
      AND user_agent = 'SQL-SEED'
);

USE pfwDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';
SET @sample_start_time = '2026-06-15 12:00:00.000';
SET @sample_end_time = '2026-06-15 12:00:00.012';

INSERT INTO pfw_transaction_log (
    LOG_DATE,
    TRANSACTION_ID,
    TRACE_ID,
    SPAN_ID,
    SEQUENCE_NO,
    MODULE_ID,
    BUSINESS_TRANSACTION_ID,
    BUSINESS_TRANSACTION_NAME,
    LOG_TYPE,
    API_VERSION,
    CLIENT_APP_ID,
    CLIENT_VERSION,
    CALLER_SERVICE,
    CALLER_INSTANCE_ID,
    CORRELATION_ID,
    IDEMPOTENCY_KEY,
    LOCALE,
    TIMEZONE,
    REQUEST_TYPE,
    ORIGINAL_CHANNEL_CODE,
    CHANNEL_CODE,
    MEMBER_NO,
    CUSTOMER_NO,
    SCREEN_ID,
    DEVICE_ID,
    WAS_ID,
    SERVER_INSTANCE_ID,
    HOST_NAME,
    PROCESS_ID,
    THREAD_NAME,
    HTTP_METHOD,
    URI,
    CONTROLLER,
    EXECUTION_PACKAGE,
    EXECUTION_CLASS,
    EXECUTION_METHOD,
    EXECUTION_SIGNATURE,
    PARAMETERS,
    REQUEST_BODY,
    RESPONSE,
    HTTP_STATUS,
    RESPONSE_CODE,
    EXEC_USER,
    CLIENT_IP,
    USER_AGENT,
    START_TIME,
    END_TIME,
    DURATION_MS,
    created_by,
    updated_by
)
SELECT
    DATE(@sample_start_time),
    @sample_transaction_id,
    'trace-sample-001',
    'span-sample-001',
    1,
    'MBR',
    'MBR01BSE0001',
    'MBR 회원 목록 샘플',
    'SUCCESS',
    'v1',
    'cpf-edu-web',
    '1.0.0',
    'xyz-education',
    'local-dev',
    'corr-sample-001',
    'idem-sample-001',
    'ko-KR',
    'Asia/Seoul',
    'NORMAL',
    'WEB',
    'WEB',
    'M000000001',
    'C000000001',
    'MBR_LIST',
    'LOCAL_BROWSER',
    'local01',
    'local-dev:sql-seed',
    'local-dev',
    'sql-seed',
    'sql-smoke',
    'GET',
    '/mbr/list',
    'cpf.mbr.bse.controller.MbrController',
    'cpf.mbr.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"memberNo":"M000000001","password":"masked"}',
    '{"code":"SPFW000000","message":"정상 처리되었습니다."}',
    200,
    'SPFW000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    @sample_start_time,
    @sample_end_time,
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'fixedTelegram', 'M000000001회원1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

USE admDB;

INSERT INTO adm_dynamic_log_level_rule (
    RULE_ID,
    TRANSACTION_ID,
    BUSINESS_TRANSACTION_ID,
    MODULE_ID,
    LOG_LEVEL,
    EXPIRE_AT,
    REASON,
    USE_YN,
    created_by,
    updated_by
) VALUES (
    'sample-rule-001',
    NULL,
    'MBR01BSE0001',
    'MBR',
    'DEBUG',
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    BUSINESS_TRANSACTION_ID = VALUES(BUSINESS_TRANSACTION_ID),
    MODULE_ID = VALUES(MODULE_ID),
    LOG_LEVEL = VALUES(LOG_LEVEL),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    REASON = VALUES(REASON),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE bizadmDB;

INSERT INTO bizadm_admin_user (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by
) VALUES (
    'biz-admin', '업무 관리자 샘플', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 'BIZ_MANAGER', 'Y', 'N',
    0, 'N', NULL, NULL, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    admin_name = VALUES(admin_name),
    password_hash = VALUES(password_hash),
    role_code = VALUES(role_code),
    use_yn = VALUES(use_yn),
    lock_yn = VALUES(lock_yn),
    login_fail_count = VALUES(login_fail_count),
    password_change_required_yn = VALUES(password_change_required_yn),
    password_expire_at = VALUES(password_expire_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_login_history (
    admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent,
    transaction_global_id, module_id, was_id, server_instance_id, created_by, updated_by
)
SELECT admin_user_id, 'BIZADM', 'biz-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260615120000000BIZbizAP010000001', 'BIZ', 'bizAP01', 'local-bizadm:seed', 'SYSTEM', 'SYSTEM'
FROM bizadm_admin_user
WHERE admin_login_id = 'biz-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM bizadm_login_history
      WHERE admin_login_id = 'biz-admin'
        AND transaction_global_id = '20260615120000000BIZbizAP010000001'
  );

INSERT INTO bizadm_menu (
    menu_code, menu_name, api_path, sort_order, use_yn, created_by, updated_by
) VALUES (
    'BIZ_CUSTOMER', '고객 업무 관리', '/api/bizadm/customers', 10, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    api_path = VALUES(api_path),
    sort_order = VALUES(sort_order),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_role (
    role_code, role_name, write_allowed_yn, use_yn, created_by, updated_by
) VALUES (
    'BIZ_MANAGER', '업무 관리자', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    write_allowed_yn = VALUES(write_allowed_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_permission (
    role_code, menu_code, button_code, allow_yn, created_by, updated_by
) VALUES
    ('BIZ_MANAGER', 'BIZ_CUSTOMER', 'READ', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BIZ_MANAGER', 'BIZ_CUSTOMER', 'WRITE', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BIZ_MANAGER', 'BIZ_CUSTOMER', 'DOWNLOAD', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    allow_yn = VALUES(allow_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_customer (
    customer_no, customer_name, email, mobile_no, customer_status, created_by, updated_by
) VALUES (
    'CUST000001', '샘플 고객', 'customer@example.com', '010-0000-0001', 'ACTIVE', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    customer_name = VALUES(customer_name),
    email = VALUES(email),
    mobile_no = VALUES(mobile_no),
    customer_status = VALUES(customer_status),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_product (
    product_code, product_name, use_yn, created_by, updated_by
) VALUES (
    'PRD_SAMPLE', '샘플 상품', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_order (
    order_no, customer_no, product_code, order_amount, order_status, created_by, updated_by
) VALUES (
    'ORD000001', 'CUST000001', 'PRD_SAMPLE', 10000.00, 'REQUESTED', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    customer_no = VALUES(customer_no),
    product_code = VALUES(product_code),
    order_amount = VALUES(order_amount),
    order_status = VALUES(order_status),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_project_setting (
    setting_key, setting_value, description, use_yn, created_by, updated_by
) VALUES (
    'bizadm.masking.enabled', 'Y', '업무 관리자 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    setting_value = VALUES(setting_value),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bizadm_masking_audit (
    target_type, target_id, operator_id, reason, result_type, created_by, updated_by
)
SELECT 'CUSTOMER', 'CUST000001', 'biz-admin', '업무 관리자 샘플 원문보기 감사', 'SUCCESS', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM bizadm_masking_audit
    WHERE target_type = 'CUSTOMER'
      AND target_id = 'CUST000001'
      AND operator_id = 'biz-admin'
      AND reason = '업무 관리자 샘플 원문보기 감사'
);

USE exsDB;

INSERT INTO exs_institution (
    institution_code, institution_name, enabled_yn, created_by, updated_by
) VALUES (
    'BANK01', '샘플 대외기관', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    institution_name = VALUES(institution_name),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_channel (
    institution_code, channel_code, direction, enabled_yn, created_by, updated_by
) VALUES (
    'BANK01', 'OPENAPI', 'OUTBOUND', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    direction = VALUES(direction),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_endpoint (
    endpoint_code, institution_code, http_method, endpoint_uri, timeout_ms, retry_count, enabled_yn, created_by, updated_by
) VALUES (
    'BANK01_BALANCE', 'BANK01', 'POST', 'https://example.invalid/balance', 3000, 2, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    institution_code = VALUES(institution_code),
    http_method = VALUES(http_method),
    endpoint_uri = VALUES(endpoint_uri),
    timeout_ms = VALUES(timeout_ms),
    retry_count = VALUES(retry_count),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_auth_profile (
    auth_profile_code, institution_code, auth_type, secret_ref, enabled_yn, created_by, updated_by
) VALUES (
    'BANK01_OAUTH', 'BANK01', 'OAUTH2', 'vault://cpf/exs/bank01/oauth', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    institution_code = VALUES(institution_code),
    auth_type = VALUES(auth_type),
    secret_ref = VALUES(secret_ref),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_token_store (
    auth_profile_code, token_key, token_hash, masked_token, token_status, issued_at, expire_at,
    transaction_global_id, server_instance_id, created_by, updated_by
) VALUES (
    'BANK01_OAUTH', 'access-token', 'HASH_ONLY_SAMPLE_NO_TOKEN_RAW', 'sample****token', 'VALID', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR),
    '20260615120000000EXSexsAP010000001', 'local-exs:seed', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    token_hash = VALUES(token_hash),
    masked_token = VALUES(masked_token),
    token_status = VALUES(token_status),
    issued_at = VALUES(issued_at),
    expire_at = VALUES(expire_at),
    transaction_global_id = VALUES(transaction_global_id),
    server_instance_id = VALUES(server_instance_id),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_token_event_history (
    auth_profile_code, token_key, event_type, reason, transaction_global_id, server_instance_id, created_by, updated_by
)
SELECT 'BANK01_OAUTH', 'access-token', 'TOKEN_REFRESH', 'SQL seed token 상태 샘플', '20260615120000000EXSexsAP010000001', 'local-exs:seed', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM exs_token_event_history
    WHERE auth_profile_code = 'BANK01_OAUTH'
      AND token_key = 'access-token'
      AND event_type = 'TOKEN_REFRESH'
      AND transaction_global_id = '20260615120000000EXSexsAP010000001'
);

INSERT INTO exs_route_rule (
    route_code, institution_code, channel_code, endpoint_code, enabled_yn, created_by, updated_by
) VALUES (
    'BANK01_BALANCE_ROUTE', 'BANK01', 'OPENAPI', 'BANK01_BALANCE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    institution_code = VALUES(institution_code),
    channel_code = VALUES(channel_code),
    endpoint_code = VALUES(endpoint_code),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

SET @sample_exs_transaction_id = '20260615120000000EXSexsAP010000001';

INSERT INTO exs_transaction_log (
    transaction_global_id, external_transaction_id, institution_code, channel_code, endpoint_code,
    module_id, was_id, server_instance_id, request_at, response_at, elapsed_ms, direction,
    http_method, request_uri, status, result_code, error_code, error_message, retryable_yn,
    created_by, updated_by
)
SELECT
    @sample_exs_transaction_id, 'EXT-20260615-0001', 'BANK01', 'OPENAPI', 'BANK01_BALANCE',
    'EXS', 'exsAP01', 'local-dev:sql-seed', @sample_start_time, @sample_end_time, 12, 'OUTBOUND',
    'POST', 'https://example.invalid/balance', 'SUCCESS', '0000', NULL, NULL, 'N',
    'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM exs_transaction_log
    WHERE transaction_global_id = @sample_exs_transaction_id
);

INSERT INTO exs_message_log (
    transaction_global_id, external_transaction_id, direction, message_summary, payload_store_yn, payload_ref, created_by, updated_by
)
SELECT @sample_exs_transaction_id, 'EXT-20260615-0001', 'OUTBOUND', '샘플 대외 송신 전문 요약', 'N', NULL, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM exs_message_log
    WHERE transaction_global_id = @sample_exs_transaction_id
      AND direction = 'OUTBOUND'
);

INSERT INTO exs_control_policy (
    institution_code, control_type, enabled_yn, reason, created_by, updated_by
) VALUES (
    'BANK01', 'SEND_BLOCK', 'N', '샘플 기관 정상 송신 허용', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    enabled_yn = VALUES(enabled_yn),
    reason = VALUES(reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO exs_retry_log (
    transaction_global_id, external_transaction_id, retry_status, retry_count, last_error_message, next_retry_at, created_by, updated_by
)
SELECT @sample_exs_transaction_id, 'EXT-20260615-0001', 'NOT_REQUIRED', 0, NULL, NULL, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM exs_retry_log
    WHERE transaction_global_id = @sample_exs_transaction_id
      AND retry_status = 'NOT_REQUIRED'
);

-- ============================================================================
-- specs/sql/99_smoke_check.sql
-- ============================================================================
-- CPF 초기 데이터베이스 smoke check입니다.
-- 01_create_databases.sql부터 70_test_data.sql까지 실행한 뒤 수행합니다.

SELECT 'pfwDB.pfw_transaction_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log;
SELECT 'pfwDB.pfw_transaction_log_detail' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log_detail;
SELECT 'pfwDB.pfw_transaction_meta' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_meta;
SELECT 'pfwDB.pfw_log_policy' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy;
SELECT 'pfwDB.pfw_log_policy_override' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy_override;
SELECT 'pfwDB.pfw_log_policy_audit' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy_audit;
SELECT 'pfwDB.pfw_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_code;
SELECT 'pfwDB.pfw_message' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_message;
SELECT 'pfwDB.pfw_response_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_response_code;
SELECT 'pfwDB.pfw_config' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_config;
SELECT 'pfwDB.pfw_cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_cache_refresh_event;
SELECT 'pfwDB.BATCH_JOB_INSTANCE' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_INSTANCE;
SELECT 'pfwDB.BATCH_JOB_EXECUTION' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION;
SELECT 'pfwDB.BATCH_JOB_EXECUTION_PARAMS' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION_PARAMS;
SELECT 'pfwDB.BATCH_STEP_EXECUTION' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_STEP_EXECUTION;
SELECT 'pfwDB.BATCH_JOB_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_SEQ;
SELECT 'pfwDB.BATCH_JOB_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION_SEQ;
SELECT 'pfwDB.BATCH_STEP_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_STEP_EXECUTION_SEQ;
SELECT 'pfwDB.pfw_batch_job' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_job;
SELECT 'pfwDB.pfw_batch_schedule' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_schedule;
SELECT 'pfwDB.pfw_batch_job_relation' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_job_relation;
SELECT 'pfwDB.pfw_batch_instance' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_instance;
SELECT 'pfwDB.pfw_batch_worker' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_worker;
SELECT 'pfwDB.pfw_batch_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_execution;
SELECT 'pfwDB.pfw_batch_execution_target' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_execution_target;
SELECT 'pfwDB.pfw_batch_step_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_step_execution;
SELECT 'pfwDB.pfw_batch_lock' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_lock;
SELECT 'pfwDB.pfw_batch_operation_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_operation_log;
SELECT 'pfwDB.pfw_batch_ghost_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_ghost_event;
SELECT 'pfwDB.pfw_center_cut_job' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_center_cut_job;
SELECT 'pfwDB.pfw_center_cut_parameter' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_center_cut_parameter;
SELECT 'pfwDB.pfw_center_cut_item' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_center_cut_item;
SELECT 'pfwDB.pfw_center_cut_result' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_center_cut_result;
SELECT 'pfwDB.pfw_business_day_calendar' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_business_day_calendar;
SELECT 'pfwDB.pfw_notification_rule' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_notification_rule;
SELECT 'pfwDB.pfw_notification_delivery_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_notification_delivery_log;

SELECT 'cmnDB.cmn_sequence' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence;
SELECT 'cmnDB.cmn_sequence_issue_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence_issue_log;
SELECT 'cmnDB.cmn_notification_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_notification_log;
SELECT 'cmnDB.cmn_business_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_business_log;
SELECT 'cmnDB.cmn_edu_query_item' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_edu_query_item;

SELECT 'admDB.adm_operator' AS check_name, COUNT(*) AS row_count FROM admDB.adm_operator;
SELECT 'admDB.adm_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_menu;
SELECT 'admDB.adm_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_button;
SELECT 'admDB.adm_role' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role;
SELECT 'admDB.adm_role_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_menu;
SELECT 'admDB.adm_role_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_button;
SELECT 'admDB.adm_api_permission' AS check_name, COUNT(*) AS row_count FROM admDB.adm_api_permission;
SELECT 'admDB.adm_role_api_permission' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_api_permission;
SELECT 'admDB.adm_password_policy' AS check_name, COUNT(*) AS row_count FROM admDB.adm_password_policy;
SELECT 'admDB.adm_audit_log' AS check_name, COUNT(*) AS row_count FROM admDB.adm_audit_log;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'mbrDB.mbr_member' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member;
SELECT 'mbrDB.mbr_member_role' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_role;
SELECT 'mbrDB.mbr_member_login_history' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_login_history;
SELECT 'mbrDB.mbr_refresh_token' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_refresh_token;

SELECT 'bizadmDB.bizadm_admin_user' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_admin_user;
SELECT 'bizadmDB.bizadm_login_history' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_login_history;
SELECT 'bizadmDB.bizadm_refresh_token' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_refresh_token;
SELECT 'bizadmDB.bizadm_menu' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_menu;
SELECT 'bizadmDB.bizadm_role' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_role;
SELECT 'bizadmDB.bizadm_permission' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_permission;
SELECT 'bizadmDB.bizadm_customer' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_customer;
SELECT 'bizadmDB.bizadm_product' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_product;
SELECT 'bizadmDB.bizadm_order' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_order;
SELECT 'bizadmDB.bizadm_project_setting' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_project_setting;
SELECT 'bizadmDB.bizadm_masking_audit' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_masking_audit;

SELECT 'exsDB.exs_institution' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_institution;
SELECT 'exsDB.exs_channel' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_channel;
SELECT 'exsDB.exs_endpoint' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_endpoint;
SELECT 'exsDB.exs_auth_profile' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_auth_profile;
SELECT 'exsDB.exs_token_store' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_token_store;
SELECT 'exsDB.exs_token_event_history' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_token_event_history;
SELECT 'exsDB.exs_route_rule' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_route_rule;
SELECT 'exsDB.exs_transaction_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_transaction_log;
SELECT 'exsDB.exs_message_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_message_log;
SELECT 'exsDB.exs_control_policy' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_control_policy;
SELECT 'exsDB.exs_retry_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_retry_log;

SELECT TRANSACTION_ID, LOG_DATE, DATE(START_TIME) AS start_date, MODULE_ID, WAS_ID, SERVER_INSTANCE_ID,
       API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CORRELATION_ID, IDEMPOTENCY_KEY
FROM pfwDB.pfw_transaction_log
WHERE TRANSACTION_ID = '20260615120000000MBRlocal010000001'
ORDER BY LOG_IDX
LIMIT 1;

SELECT COUNT(*) AS transaction_log_date_mismatch_count
FROM pfwDB.pfw_transaction_log
WHERE START_TIME IS NOT NULL
  AND LOG_DATE <> DATE(START_TIME);

SELECT DETAIL_KEY, DETAIL_VALUE
FROM pfwDB.pfw_transaction_log_detail
WHERE DETAIL_KEY IN ('headers', 'fixedTelegram')
ORDER BY DETAIL_KEY;

SELECT sequence_key, business_area, business_key, sequence_kind, channel_code, start_value, increment_by, reset_cycle, log_enabled_yn
FROM cmnDB.cmn_sequence
WHERE sequence_key = 'CMN_EDU_ORDER';

SELECT item_id, item_name, category_code, status_code, owner_member_no
FROM cmnDB.cmn_edu_query_item
WHERE use_yn = 'Y'
ORDER BY item_id
LIMIT 5;

SELECT AUDIT_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, IMMUTABLE_YN
FROM admDB.adm_audit_log
ORDER BY AUDIT_ID DESC
LIMIT 5;

SELECT ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN
FROM admDB.adm_role_menu
WHERE MENU_ID IN ('MEMBER', 'BATCH', 'PERMISSION')
ORDER BY ROLE_ID, MENU_ID;

SELECT ROLE_ID, BUTTON_ID, ALLOW_YN
FROM admDB.adm_role_button
WHERE BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_ROLE_GRANT', 'BATCH_EXECUTE', 'BATCH_CALENDAR_SAVE', 'BATCH_SIMULATION', 'BATCH_TARGET_READ')
ORDER BY ROLE_ID, BUTTON_ID;

SELECT ROLE_ID, API_PERMISSION_ID, ALLOW_YN
FROM admDB.adm_role_api_permission
WHERE API_PERMISSION_ID IN ('API_PERMISSION_READ', 'API_PERMISSION_WRITE_PUT', 'API_OPERATOR_READ')
ORDER BY ROLE_ID, API_PERMISSION_ID;

SELECT schedule_id, job_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern
FROM pfwDB.pfw_batch_schedule
ORDER BY schedule_id;

SELECT job_id, related_job_id, relation_type, trigger_condition, required_status
FROM pfwDB.pfw_batch_job_relation
ORDER BY job_id, related_job_id;

SELECT job_id, schedule_id, target_instance_id, business_date, dispatch_status
FROM pfwDB.pfw_batch_execution_target
ORDER BY target_id
LIMIT 5;

SELECT worker_id, server_instance_id, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id
FROM pfwDB.pfw_batch_worker
ORDER BY worker_id
LIMIT 5;

SELECT execution_id, job_id, execution_status, spring_batch_execution_id, batch_instance_id, server_instance_id,
       worker_id, transaction_global_id, requested_by
FROM pfwDB.pfw_batch_execution
ORDER BY execution_id DESC
LIMIT 5;

SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status
FROM pfwDB.pfw_batch_step_execution
ORDER BY step_execution_id DESC
LIMIT 5;

SELECT ghost_event_id, execution_id, job_id, worker_id, ghost_status, action_type, lock_released_yn, retryable_yn
FROM pfwDB.pfw_batch_ghost_event
ORDER BY ghost_event_id DESC
LIMIT 5;

SELECT member_no, customer_no, login_id, name, member_status, lock_yn, withdraw_yn
FROM mbrDB.mbr_member
ORDER BY id
LIMIT 5;

SELECT login_domain, member_no, customer_no, login_id, login_result, transaction_global_id, module_id, was_id, server_instance_id
FROM mbrDB.mbr_member_login_history
ORDER BY login_history_id DESC
LIMIT 5;

SELECT admin_login_id, role_code, use_yn, lock_yn, login_fail_count
FROM bizadmDB.bizadm_admin_user
ORDER BY admin_user_id
LIMIT 5;

SELECT login_domain, admin_login_id, login_result, transaction_global_id, module_id, was_id, server_instance_id
FROM bizadmDB.bizadm_login_history
ORDER BY login_history_id DESC
LIMIT 5;

SELECT member_id, service_code, role_code, role_name, use_yn
FROM mbrDB.mbr_member_role
ORDER BY member_role_id
LIMIT 5;

SELECT auth_profile_code, token_key, token_status, masked_token, transaction_global_id, server_instance_id
FROM exsDB.exs_token_store
ORDER BY token_id
LIMIT 5;

SELECT auth_profile_code, token_key, event_type, transaction_global_id, server_instance_id
FROM exsDB.exs_token_event_history
ORDER BY token_event_id DESC
LIMIT 5;

SELECT response_code, message_code, result_type, http_status
FROM pfwDB.pfw_response_code
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM pfwDB.pfw_message
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;
