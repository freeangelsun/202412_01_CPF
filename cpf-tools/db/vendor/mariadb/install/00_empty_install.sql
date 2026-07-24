-- CPF generated SQL bundle: 00_empty_install.sql
-- 목적: 빈 Schema에 제품 Object만 비파괴 설치
-- 정본은 specs/sql의 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File scripts/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- specs/sql/10_cpf_schema.sql
-- ============================================================================
-- CPF 프레임워크 엔진 스키마입니다.
-- 거래로그, 시스템 코드/메시지, 응답코드, 설정, 캐시 이벤트, 보안 메타, 배치 운영 메타를 cpfDB에 배치합니다.

USE cpfDB;

CREATE TABLE IF NOT EXISTS cpf_schema_installation (
    schema_name VARCHAR(64) NOT NULL COMMENT 'CPF 소유 Schema 이름',
    system_code VARCHAR(20) NOT NULL COMMENT '공식 SystemCode',
    database_vendor VARCHAR(20) NOT NULL COMMENT '설치 DB Vendor',
    product_version VARCHAR(50) NOT NULL COMMENT 'CPF 제품 버전',
    baseline_key VARCHAR(100) NOT NULL COMMENT 'Empty Install Baseline 식별자',
    install_state VARCHAR(30) NOT NULL COMMENT '설치 상태',
    installed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 설치 완료 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF_INSTALLER' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF_INSTALLER' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (schema_name),
    INDEX ix_cpf_schema_installation_system (system_code),
    INDEX ix_cpf_schema_installation_version (product_version, install_state),
    CONSTRAINT ck_cpf_schema_installation_vendor
        CHECK (database_vendor IN ('MARIADB', 'MYSQL', 'POSTGRESQL', 'ORACLE', 'SQLSERVER')),
    CONSTRAINT ck_cpf_schema_installation_state
        CHECK (install_state IN ('PRODUCT_SEEDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='CPF 공식 Empty Install 및 Product Seed Baseline';

CREATE TABLE IF NOT EXISTS cpf_transaction_log (
    LOG_DATE DATE NOT NULL COMMENT '로그 기준일',
    LOG_IDX BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 로그 순번',
    RECOVERY_EVENT_ID VARCHAR(64) NULL COMMENT 'DB 로그 복구 이벤트 중복 방지 ID',
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (LOG_IDX),
    UNIQUE KEY uk_cpf_transaction_log_recovery_event (RECOVERY_EVENT_ID),
    INDEX ix_cpf_transaction_log_date (LOG_DATE),
    INDEX ix_cpf_transaction_log_transaction_time (TRANSACTION_ID, START_TIME, LOG_IDX),
    INDEX ix_cpf_transaction_log_trace_id (TRACE_ID),
    INDEX ix_cpf_transaction_log_business_time (BUSINESS_TRANSACTION_ID, START_TIME),
    INDEX ix_cpf_transaction_log_client_app (CLIENT_APP_ID, START_TIME),
    INDEX ix_cpf_transaction_log_correlation (CORRELATION_ID, START_TIME),
    INDEX ix_cpf_transaction_log_idempotency (IDEMPOTENCY_KEY),
    INDEX ix_cpf_transaction_log_member_time (MEMBER_NO, START_TIME),
    INDEX ix_cpf_transaction_log_customer_time (CUSTOMER_NO, START_TIME),
    INDEX ix_cpf_transaction_log_channel_time (CHANNEL_CODE, START_TIME),
    INDEX ix_cpf_transaction_log_module_time (MODULE_ID, START_TIME),
    INDEX ix_cpf_transaction_log_server_time (SERVER_INSTANCE_ID, START_TIME),
    INDEX ix_cpf_transaction_log_status_time (LOG_TYPE, RESPONSE_CODE, START_TIME),
    INDEX ix_cpf_transaction_log_http_status_time (HTTP_STATUS, START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 거래 요약 로그';

CREATE TABLE IF NOT EXISTS cpf_transaction_log_detail (
    DETAIL_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 상세 로그 순번',
    LOG_IDX BIGINT NOT NULL COMMENT '거래 로그 순번',
    DETAIL_KEY VARCHAR(100) NOT NULL DEFAULT 'N/A' COMMENT '상세 항목 키',
    DETAIL_VALUE MEDIUMTEXT NOT NULL COMMENT '상세 항목 값',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (DETAIL_ID),
    CONSTRAINT fk_cpf_transaction_log_detail_log
        FOREIGN KEY (LOG_IDX) REFERENCES cpf_transaction_log(LOG_IDX)
        ON DELETE CASCADE,
    INDEX ix_cpf_transaction_log_detail_log_key (LOG_IDX, DETAIL_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 거래 상세 로그';

CREATE TABLE IF NOT EXISTS cpf_transaction_segment (
    segment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 구간 내부 순번',
    transaction_segment_id VARCHAR(120) NOT NULL COMMENT '거래 구간 ID',
    transaction_global_id VARCHAR(100) NOT NULL COMMENT '전체 거래 묶음 ID',
    root_transaction_global_id VARCHAR(100) NULL COMMENT '최초 진입 거래 ID',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID',
    transaction_role VARCHAR(40) NOT NULL COMMENT '구간 역할',
    module_code VARCHAR(20) NOT NULL COMMENT '현재 처리 모듈 코드',
    source_module_code VARCHAR(20) NULL COMMENT '호출 출발 모듈 코드',
    target_module_code VARCHAR(20) NULL COMMENT '호출 대상 모듈 코드',
    direction VARCHAR(20) NOT NULL COMMENT '구간 처리 방향',
    call_depth INT NOT NULL DEFAULT 0 COMMENT '호출 깊이',
    sequence_no INT NOT NULL DEFAULT 1 COMMENT '거래 내 구간 순번',
    api_path VARCHAR(500) NULL COMMENT '처리 API 경로',
    transaction_name VARCHAR(200) NULL COMMENT '거래 구간명',
    started_at DATETIME(6) NOT NULL COMMENT '구간 시작 일시',
    ended_at DATETIME(6) NULL COMMENT '구간 종료 일시',
    duration_ms BIGINT NULL COMMENT '구간 수행시간 밀리초',
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT '구간 처리 상태',
    failure_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실패 여부',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message_masked VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    request_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 요청 헤더 snapshot',
    response_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 응답 헤더 snapshot',
    extension_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 확장 헤더 snapshot',
    customer_no_masked VARCHAR(80) NULL COMMENT '마스킹된 고객번호',
    member_no_masked VARCHAR(80) NULL COMMENT '마스킹된 회원번호',
    user_id_masked VARCHAR(80) NULL COMMENT '마스킹된 사용자 ID',
    operator_id_masked VARCHAR(80) NULL COMMENT '마스킹된 운영자 ID',
    channel_code VARCHAR(30) NULL COMMENT '현재 채널 코드',
    original_channel_code VARCHAR(30) NULL COMMENT '최초 유입 채널 코드',
    client_app_id VARCHAR(100) NULL COMMENT '클라이언트 애플리케이션 ID',
    caller_service VARCHAR(100) NULL COMMENT '호출 서비스 ID',
    external_institution_code VARCHAR(50) NULL COMMENT '외부기관 코드',
    external_transaction_id VARCHAR(120) NULL COMMENT '외부기관 거래 ID',
    selected_instance_id VARCHAR(100) NULL COMMENT '선택된 하위 서비스 인스턴스 ID',
    attempt_no INT NULL COMMENT '서비스 호출 시도 순번',
    retry_yn CHAR(1) NULL COMMENT '재시도 여부',
    failover_yn CHAR(1) NULL COMMENT '다른 인스턴스로 전환한 여부',
    circuit_state VARCHAR(20) NULL COMMENT '호출 시점 circuit 상태',
    downstream_http_status INT NULL COMMENT '하위 서비스 HTTP 상태',
    result_state VARCHAR(30) NULL COMMENT '호출 결과 상태',
    unknown_result_id VARCHAR(100) NULL COMMENT '결과 미확정 관리 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (segment_id),
    UNIQUE KEY uk_cpf_transaction_segment_id (transaction_segment_id),
    INDEX ix_cpf_transaction_segment_global (transaction_global_id, started_at, segment_id),
    INDEX ix_cpf_transaction_segment_parent (parent_segment_id),
    INDEX ix_cpf_transaction_segment_module (module_code, started_at),
    INDEX ix_cpf_transaction_segment_role (transaction_role, direction),
    INDEX ix_cpf_transaction_segment_status (failure_yn, status, started_at),
    INDEX ix_cpf_transaction_segment_duration (duration_ms),
    INDEX ix_cpf_transaction_segment_customer (customer_no_masked, started_at),
    INDEX ix_cpf_transaction_segment_member (member_no_masked, started_at),
    INDEX ix_cpf_transaction_segment_user (user_id_masked, started_at),
    INDEX ix_cpf_transaction_segment_operator (operator_id_masked, started_at),
    INDEX ix_cpf_transaction_segment_client (client_app_id, caller_service, started_at),
    INDEX ix_cpf_transaction_segment_external (external_institution_code, external_transaction_id),
    INDEX ix_cpf_transaction_segment_instance (selected_instance_id, started_at),
    INDEX ix_cpf_transaction_segment_attempt (transaction_global_id, attempt_no),
    INDEX ix_cpf_transaction_segment_unknown (unknown_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 복합 거래 구간 로그';

CREATE TABLE IF NOT EXISTS cpf_service (
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    service_name VARCHAR(150) NOT NULL COMMENT '서비스명',
    service_type VARCHAR(30) NOT NULL DEFAULT 'INTERNAL' COMMENT '서비스 유형',
    owner_module_code VARCHAR(20) NOT NULL COMMENT '소유 모듈 코드',
    description VARCHAR(500) NULL COMMENT '서비스 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (service_id),
    INDEX ix_cpf_service_owner (owner_module_code, use_yn),
    INDEX ix_cpf_service_type (service_type, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 레지스트리';

CREATE TABLE IF NOT EXISTS cpf_service_endpoint (
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_name VARCHAR(150) NOT NULL COMMENT 'Endpoint명',
    endpoint_type VARCHAR(30) NOT NULL DEFAULT 'HTTP' COMMENT 'Endpoint 유형',
    base_url VARCHAR(500) NOT NULL COMMENT '기본 URL',
    context_path VARCHAR(200) NULL COMMENT 'Context path',
    default_timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '기본 timeout 밀리초',
    default_retry_count INT NOT NULL DEFAULT 0 COMMENT '기본 retry 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (endpoint_code),
    INDEX ix_cpf_service_endpoint_service (service_id, use_yn),
    INDEX ix_cpf_service_endpoint_type (endpoint_type, use_yn),
    CONSTRAINT fk_cpf_service_endpoint_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Endpoint 레지스트리';

CREATE TABLE IF NOT EXISTS cpf_service_instance (
    instance_id VARCHAR(120) NOT NULL COMMENT '서비스 인스턴스 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_name VARCHAR(150) NOT NULL COMMENT '서비스 인스턴스명',
    base_url VARCHAR(500) NOT NULL COMMENT '인스턴스 기본 URL',
    host_name VARCHAR(150) NULL COMMENT 'Host명',
    port_no INT NULL COMMENT 'Port 번호',
    instance_status VARCHAR(30) NOT NULL DEFAULT 'UP' COMMENT '인스턴스 상태',
    weight INT NOT NULL DEFAULT 100 COMMENT '라우팅 가중치',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (instance_id),
    INDEX ix_cpf_service_instance_endpoint (service_id, endpoint_code, active_yn, instance_status),
    INDEX ix_cpf_service_instance_weight (endpoint_code, weight),
    CONSTRAINT fk_cpf_service_instance_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id),
    CONSTRAINT fk_cpf_service_instance_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES cpf_service_endpoint(endpoint_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 인스턴스 레지스트리';

CREATE TABLE IF NOT EXISTS cpf_service_health_status (
    health_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 health 이력 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    health_status VARCHAR(30) NOT NULL COMMENT 'Health 상태',
    http_status INT NULL COMMENT 'HTTP 상태 코드',
    response_time_ms BIGINT NULL COMMENT '응답 시간 밀리초',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    checked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '점검 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (health_id),
    INDEX ix_cpf_service_health_target (service_id, endpoint_code, instance_id, checked_at),
    INDEX ix_cpf_service_health_status (health_status, checked_at),
    CONSTRAINT fk_cpf_service_health_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id),
    CONSTRAINT fk_cpf_service_health_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES cpf_service_endpoint(endpoint_code),
    CONSTRAINT fk_cpf_service_health_instance
        FOREIGN KEY (instance_id) REFERENCES cpf_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Health 상태 이력';

CREATE TABLE IF NOT EXISTS cpf_service_routing_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '라우팅 정책 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    routing_mode VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT '라우팅 모드',
    load_balance_type VARCHAR(30) NOT NULL DEFAULT 'WEIGHT' COMMENT '부하 분산 유형',
    failover_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Failover 사용 여부',
    health_check_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Health check 필수 여부',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    priority INT NOT NULL DEFAULT 100 COMMENT '우선순위',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_cpf_service_routing_policy (service_id, endpoint_code, priority),
    INDEX ix_cpf_service_routing_active (service_id, endpoint_code, active_yn, priority),
    CONSTRAINT fk_cpf_service_routing_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id),
    CONSTRAINT fk_cpf_service_routing_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES cpf_service_endpoint(endpoint_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 라우팅 정책';

CREATE TABLE IF NOT EXISTS cpf_service_circuit_state (
    circuit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Circuit 상태 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    circuit_state VARCHAR(30) NOT NULL DEFAULT 'CLOSED' COMMENT 'Circuit 상태',
    failure_count INT NOT NULL DEFAULT 0 COMMENT '실패 횟수',
    success_count INT NOT NULL DEFAULT 0 COMMENT '성공 횟수',
    opened_at DATETIME(3) NULL COMMENT 'Open 일시',
    half_opened_at DATETIME(3) NULL COMMENT 'Half-open 일시',
    closed_at DATETIME(3) NULL COMMENT 'Close 일시',
    last_failure_message VARCHAR(1000) NULL COMMENT '마지막 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (circuit_id),
    UNIQUE KEY uk_cpf_service_circuit_state (service_id, endpoint_code, instance_id),
    INDEX ix_cpf_service_circuit_state (circuit_state, updated_at),
    CONSTRAINT fk_cpf_service_circuit_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id),
    CONSTRAINT fk_cpf_service_circuit_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES cpf_service_endpoint(endpoint_code),
    CONSTRAINT fk_cpf_service_circuit_instance
        FOREIGN KEY (instance_id) REFERENCES cpf_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Circuit 상태';

CREATE TABLE IF NOT EXISTS cpf_service_call_history (
    call_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 호출 이력 ID',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT 'Trace ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET' COMMENT 'HTTP Method',
    request_path VARCHAR(500) NOT NULL DEFAULT '/' COMMENT '요청 경로',
    call_status VARCHAR(30) NOT NULL COMMENT '호출 상태',
    http_status INT NULL COMMENT 'HTTP 상태 코드',
    duration_ms BIGINT NULL COMMENT '소요 시간 밀리초',
    timeout_ms INT NULL COMMENT 'Timeout 밀리초',
    retry_count INT NULL COMMENT 'Retry 횟수',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (call_id),
    INDEX ix_cpf_service_call_history_tx (transaction_global_id, call_id),
    INDEX ix_cpf_service_call_history_service (service_id, endpoint_code, created_at),
    INDEX ix_cpf_service_call_history_status (call_status, created_at),
    CONSTRAINT fk_cpf_service_call_history_service
        FOREIGN KEY (service_id) REFERENCES cpf_service(service_id),
    CONSTRAINT fk_cpf_service_call_history_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES cpf_service_endpoint(endpoint_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_cpf_service_call_history_instance
        FOREIGN KEY (instance_id) REFERENCES cpf_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 호출 이력';

CREATE TABLE IF NOT EXISTS cpf_transaction_meta (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (transaction_id),
    INDEX ix_cpf_transaction_meta_module (module_code, domain_code, active_yn),
    INDEX ix_cpf_transaction_meta_path (http_method, api_path),
    INDEX ix_cpf_transaction_meta_policy (log_policy_key, active_yn),
    INDEX ix_cpf_transaction_meta_scan (active_yn, last_scanned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 온라인 거래 메타';

CREATE TABLE IF NOT EXISTS cpf_standard_execution (
    standard_execution_id CHAR(10) NOT NULL COMMENT 'CPF O·S·B 10자리 표준 실행 ID',
    execution_name VARCHAR(150) NOT NULL COMMENT '표준 실행명',
    execution_type VARCHAR(20) NOT NULL COMMENT '실행 유형 ONLINE, SHARED 또는 BATCH',
    owner_domain VARCHAR(20) NOT NULL COMMENT '실행 소유 주제영역',
    source_module VARCHAR(20) NOT NULL COMMENT '발견 소스 모듈',
    source_class VARCHAR(255) NOT NULL COMMENT '선언 클래스명',
    source_method VARCHAR(150) NOT NULL COMMENT '선언 메서드명',
    http_method VARCHAR(10) NULL COMMENT 'HTTP 진입 method',
    endpoint VARCHAR(500) NULL COMMENT '연결 API 또는 배치 endpoint',
    operation_id VARCHAR(150) NULL COMMENT '연결 OpenAPI operation ID',
    description VARCHAR(1000) NULL COMMENT '실행 기능 설명',
    required_permission VARCHAR(150) NULL COMMENT '필수 실행 권한 코드',
    audit_reason_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '감사 사유 필수 여부',
    visibility VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' COMMENT 'PUBLIC 또는 INTERNAL 노출 범위',
    direct_allowed_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '업무 URL 직접 호출 허용 여부',
    gateway_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공개 CPF Gateway 호출 허용 여부',
    source_version VARCHAR(100) NOT NULL COMMENT '소스 버전 또는 빌드 식별자',
    registration_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED' COMMENT '등록 상태',
    first_registered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 등록일시',
    last_discovered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 발견일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (standard_execution_id),
    INDEX ix_cpf_standard_execution_type (execution_type, registration_status),
    INDEX ix_cpf_standard_execution_owner (owner_domain, source_module),
    INDEX ix_cpf_standard_execution_source (source_class, source_method),
    CONSTRAINT ck_cpf_standard_execution_id CHECK (
        standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
        AND RIGHT(standard_execution_id, 4) <> '0000'
    ),
    CONSTRAINT ck_cpf_standard_execution_type CHECK (execution_type IN ('ONLINE', 'SHARED', 'BATCH'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF O·S·B 표준 실행 카탈로그';

CREATE TABLE IF NOT EXISTS cpf_standard_execution_alias (
    legacy_execution_id VARCHAR(32) NOT NULL COMMENT '조회 호환용 구형 실행 ID',
    standard_execution_id CHAR(10) NOT NULL COMMENT '현재 10자리 표준 실행 ID',
    migration_reason VARCHAR(300) NOT NULL COMMENT 'ID 전환 사유',
    retired_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '구형 ID 사용 종료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (legacy_execution_id),
    UNIQUE KEY uk_cpf_standard_execution_alias_current (standard_execution_id, legacy_execution_id),
    CONSTRAINT ck_cpf_standard_execution_alias_current CHECK (
        standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 구형 실행 ID 조회 호환 이력';

CREATE TABLE IF NOT EXISTS cpf_channel_policy_version (
    version_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '채널 정책 스냅샷 버전',
    change_type VARCHAR(30) NOT NULL COMMENT 'CHANNEL 또는 EXECUTION_POLICY 변경 유형',
    target_key VARCHAR(100) NOT NULL COMMENT '변경 대상 채널 또는 정책 키',
    change_reason VARCHAR(500) NOT NULL COMMENT '운영 변경 사유',
    applied_by VARCHAR(100) NOT NULL COMMENT '적용 운영자',
    applied_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '적용일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (version_id),
    INDEX ix_cpf_channel_policy_version_target (change_type, target_key, version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 채널 정책 변경 버전';

CREATE TABLE IF NOT EXISTS cpf_channel_registry (
    channel_code VARCHAR(30) NOT NULL COMMENT 'CPF 통합 채널 코드',
    channel_name VARCHAR(100) NOT NULL COMMENT '채널명',
    channel_type VARCHAR(30) NOT NULL COMMENT 'CLIENT, OPERATOR 또는 SYSTEM 유형',
    trust_level VARCHAR(30) NOT NULL COMMENT 'EXTERNAL 또는 INTERNAL 신뢰 수준',
    client_channel_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '최초 유입 클라이언트 채널 여부',
    internal_channel_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '내부 호출 채널 여부',
    authentication_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '인증 필수 여부',
    signature_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '요청 서명 필수 여부',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '채널 사용 여부',
    description VARCHAR(500) NULL COMMENT '채널 설명',
    policy_version BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적용 정책 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (channel_code),
    INDEX ix_cpf_channel_registry_active (active_yn, channel_type),
    CONSTRAINT ck_cpf_channel_registry_client CHECK (client_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_internal CHECK (internal_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_active CHECK (active_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 통합 채널 레지스트리';

CREATE TABLE IF NOT EXISTS cpf_channel_execution_policy (
    policy_key VARCHAR(100) NOT NULL COMMENT '채널 실행 정책 불변 키',
    standard_execution_id VARCHAR(10) NOT NULL COMMENT '10자리 표준 실행 ID 또는 전체 실행 *',
    original_channel_code VARCHAR(30) NOT NULL COMMENT '최초 채널 코드 또는 ANY',
    caller_channel_code VARCHAR(30) NOT NULL COMMENT '현재 호출 채널 코드 또는 ANY',
    request_type VARCHAR(30) NOT NULL DEFAULT '*' COMMENT '요청 유형 또는 전체 유형 *',
    allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실행 허용 여부',
    authentication_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '정책별 인증 필수 여부',
    signature_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '정책별 요청 서명 필수 여부',
    max_tps INT NOT NULL DEFAULT 0 COMMENT '0이면 제한하지 않는 최대 초당 요청 수',
    effective_from DATETIME(3) NULL COMMENT '정책 적용 시작일시',
    effective_to DATETIME(3) NULL COMMENT '정책 적용 종료일시',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '정책 사용 여부',
    policy_version BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적용 정책 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_key),
    INDEX ix_cpf_channel_execution_policy_lookup (
        standard_execution_id, original_channel_code, caller_channel_code, request_type, active_yn
    ),
    INDEX ix_cpf_channel_execution_policy_effective (active_yn, effective_from, effective_to),
    CONSTRAINT fk_cpf_channel_execution_policy_original FOREIGN KEY (original_channel_code)
        REFERENCES cpf_channel_registry(channel_code),
    CONSTRAINT fk_cpf_channel_execution_policy_caller FOREIGN KEY (caller_channel_code)
        REFERENCES cpf_channel_registry(channel_code),
    CONSTRAINT ck_cpf_channel_execution_policy_execution CHECK (
        standard_execution_id = '*'
        OR standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
    ),
    CONSTRAINT ck_cpf_channel_execution_policy_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_active CHECK (active_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_period CHECK (
        effective_from IS NULL OR effective_to IS NULL OR effective_from <= effective_to
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 표준 실행별 최초·호출 채널 정책';

CREATE TABLE IF NOT EXISTS cpf_log_policy (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_cpf_log_policy_key (policy_key),
    UNIQUE KEY uk_cpf_log_policy_target (target_type, target_id),
    INDEX ix_cpf_log_policy_active (active_yn, target_type, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책';

CREATE TABLE IF NOT EXISTS cpf_log_policy_override (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (override_id),
    INDEX ix_cpf_log_policy_override_target (target_type, target_id, active_yn),
    INDEX ix_cpf_log_policy_override_period (effective_start_at, effective_end_at, active_yn),
    INDEX ix_cpf_log_policy_override_policy (policy_id, active_yn),
    CONSTRAINT fk_cpf_log_policy_override_policy
        FOREIGN KEY (policy_id) REFERENCES cpf_log_policy(policy_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책 임시 override';

CREATE TABLE IF NOT EXISTS cpf_log_policy_audit (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (audit_id),
    INDEX ix_cpf_log_policy_audit_target (target_type, target_id, created_at),
    INDEX ix_cpf_log_policy_audit_operator (operator_id, created_at),
    INDEX ix_cpf_log_policy_audit_policy (policy_id, created_at),
    CONSTRAINT fk_cpf_log_policy_audit_policy
        FOREIGN KEY (policy_id) REFERENCES cpf_log_policy(policy_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cpf_log_policy_audit_override
        FOREIGN KEY (override_id) REFERENCES cpf_log_policy_override(override_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책 감사 로그';

CREATE TABLE IF NOT EXISTS cpf_code (
    code_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '코드 순번',
    parent_id BIGINT NULL COMMENT '상위 코드 순번',
    code_key VARCHAR(80) NOT NULL COMMENT '코드 그룹 키',
    code_value VARCHAR(120) NOT NULL COMMENT '코드 값',
    description VARCHAR(500) NULL COMMENT '코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (code_id),
    CONSTRAINT fk_cpf_code_parent
        FOREIGN KEY (parent_id) REFERENCES cpf_code(code_id)
        ON DELETE SET NULL,
    UNIQUE KEY uk_cpf_code_key_value (code_key, code_value),
    INDEX ix_cpf_code_parent (parent_id),
    INDEX ix_cpf_code_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 공통 코드';

CREATE TABLE IF NOT EXISTS cpf_message (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (message_id),
    UNIQUE KEY uk_cpf_message_code_locale (message_code, locale),
    INDEX ix_cpf_message_code_use (message_code, use_yn),
    INDEX ix_cpf_message_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 메시지';

CREATE TABLE IF NOT EXISTS cpf_response_code (
    response_code VARCHAR(20) NOT NULL COMMENT 'CPF 응답 코드',
    message_code VARCHAR(20) NOT NULL COMMENT '연결 메시지 코드',
    result_type CHAR(1) NOT NULL COMMENT '결과 유형',
    module_id VARCHAR(3) NOT NULL COMMENT '모듈 ID',
    response_group VARCHAR(2) NOT NULL COMMENT '응답 그룹',
    sequence_no VARCHAR(4) NOT NULL COMMENT '응답 일련번호',
    http_status INT NOT NULL COMMENT 'HTTP 상태 코드',
    description VARCHAR(500) NULL COMMENT '응답 코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (response_code),
    INDEX ix_cpf_response_code_message (message_code),
    INDEX ix_cpf_response_code_module (module_id, result_type, response_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 응답 코드';

CREATE TABLE IF NOT EXISTS cpf_config (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '설정 순번',
    config_key VARCHAR(150) NOT NULL COMMENT '설정 키',
    config_value VARCHAR(2000) NOT NULL COMMENT '설정 값',
    config_type VARCHAR(30) NOT NULL DEFAULT 'STRING' COMMENT '설정 값 유형',
    description VARCHAR(500) NULL COMMENT '설정 설명',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_cpf_config_key (config_key),
    INDEX ix_cpf_config_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 설정';

CREATE TABLE IF NOT EXISTS cpf_cache_refresh_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '캐시 갱신 이벤트 순번',
    cache_name VARCHAR(50) NOT NULL COMMENT '캐시 이름',
    event_type VARCHAR(30) NOT NULL COMMENT '이벤트 유형',
    event_key VARCHAR(200) NULL COMMENT '이벤트 대상 키',
    source_was_id VARCHAR(50) NULL COMMENT '이벤트 발행 WAS ID',
    published_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '발행자',
    published_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발행일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (event_id),
    INDEX ix_cpf_cache_refresh_event_cache_id (cache_name, event_id),
    INDEX ix_cpf_cache_refresh_event_time (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 캐시 갱신 DB fallback 이벤트';

CREATE TABLE IF NOT EXISTS cpf_security_jwt_key (
    KEY_ID VARCHAR(80) NOT NULL COMMENT 'JWT key ID',
    ISSUER VARCHAR(100) NOT NULL COMMENT '토큰 발급자',
    ALGORITHM VARCHAR(20) NOT NULL DEFAULT 'HS256' COMMENT '서명 알고리즘',
    SECRET_REF VARCHAR(500) NOT NULL COMMENT 'Vault/KMS/환경변수 secret 참조',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    EXPIRE_AT DATETIME NULL COMMENT '만료일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (KEY_ID),
    INDEX ix_cpf_security_jwt_key_issuer (ISSUER, ACTIVE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF JWT key 메타';

CREATE TABLE IF NOT EXISTS cpf_security_token_audit_log (
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
    created_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (TOKEN_AUDIT_ID),
    INDEX ix_cpf_security_token_tx (TRANSACTION_ID),
    INDEX ix_cpf_security_token_hash (TOKEN_HASH),
    INDEX ix_cpf_security_token_subject_time (SUBJECT, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 보안 토큰 감사 로그';

CREATE TABLE IF NOT EXISTS cpf_notification_rule (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    event_sub_type VARCHAR(80) NULL COMMENT '알림 이벤트 세부 유형',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ADM' COMMENT '알림 채널 코드',
    template_code VARCHAR(80) NULL COMMENT '알림 템플릿 코드',
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '알림 심각도',
    receiver_group VARCHAR(100) NULL COMMENT '수신자 그룹',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_cpf_notification_rule (event_type, event_sub_type, channel_code),
    INDEX ix_cpf_notification_rule_use (use_yn, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 규칙';

CREATE TABLE IF NOT EXISTS cpf_notification_delivery_log (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (delivery_id),
    INDEX ix_cpf_notification_delivery_target (target_type, target_id, requested_at),
    INDEX ix_cpf_notification_delivery_status (delivery_status, requested_at),
    CONSTRAINT fk_cpf_notification_delivery_rule
        FOREIGN KEY (rule_id) REFERENCES cpf_notification_rule(rule_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 발송 로그';

CREATE TABLE IF NOT EXISTS cpf_idempotency_record (
    idempotency_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '중복 처리 내부 순번',
    scope VARCHAR(40) NOT NULL COMMENT '중복 처리 적용 범위',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '중복 처리 키',
    request_hash VARCHAR(128) NULL COMMENT '요청 본문 해시',
    payload_hash VARCHAR(128) NULL COMMENT '처리 대상 payload 해시',
    record_status VARCHAR(30) NOT NULL DEFAULT 'PROCESSING' COMMENT '중복 처리 상태',
    stored_response MEDIUMTEXT NULL COMMENT '재응답용 저장 응답',
    retry_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '재요청 허용 여부',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    expires_at DATETIME(3) NULL COMMENT '만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (idempotency_seq),
    UNIQUE KEY uk_cpf_idempotency_record_key (scope, idempotency_key),
    INDEX ix_cpf_idempotency_record_status (record_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 중복 처리 기록';

CREATE TABLE IF NOT EXISTS cpf_broker_outbox (
    outbox_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Outbox 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    topic VARCHAR(160) NOT NULL COMMENT 'Broker topic 또는 queue',
    message_key VARCHAR(200) NULL COMMENT 'Broker partition key',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    producer_module VARCHAR(20) NULL COMMENT '생산 모듈',
    consumer_module VARCHAR(20) NULL COMMENT '소비 모듈',
    idempotency_key VARCHAR(160) NULL COMMENT '중복 처리 키',
    payload LONGBLOB NULL COMMENT '메시지 payload',
    content_type VARCHAR(100) NULL COMMENT '메시지 content type',
    header_json MEDIUMTEXT NULL COMMENT '메시지 header 직렬화 값',
    attribute_json MEDIUMTEXT NULL COMMENT '메시지 속성 직렬화 값',
    outbox_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'Outbox 처리 상태',
    worker_id VARCHAR(120) NULL COMMENT '처리 worker ID',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '발행 시도 횟수',
    max_attempts INT NOT NULL DEFAULT 5 COMMENT '최대 발행 시도 횟수',
    next_attempt_at DATETIME(3) NULL COMMENT '다음 발행 가능 일시',
    lease_until DATETIME(3) NULL COMMENT 'worker 점유 만료 일시',
    broker_name VARCHAR(80) NULL COMMENT '전송 대상 broker 이름',
    partition_key VARCHAR(200) NULL COMMENT '전송 partition key',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '업무 이벤트 발생 일시',
    claimed_at DATETIME(3) NULL COMMENT 'worker 점유 일시',
    published_at DATETIME(3) NULL COMMENT '발행 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (outbox_id),
    UNIQUE KEY uk_cpf_broker_outbox_message (message_id),
    INDEX ix_cpf_broker_outbox_status (outbox_status, outbox_id),
    INDEX ix_cpf_broker_outbox_ready (outbox_status, next_attempt_at, outbox_id),
    INDEX ix_cpf_broker_outbox_lease (outbox_status, lease_until),
    INDEX ix_cpf_broker_outbox_tx (transaction_global_id, segment_id),
    INDEX ix_cpf_broker_outbox_topic (topic, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker Outbox';

CREATE TABLE IF NOT EXISTS cpf_broker_inbox (
    inbox_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Inbox 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    idempotency_key VARCHAR(160) NULL COMMENT '중복 처리 키',
    inbox_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED' COMMENT 'Inbox 처리 상태',
    result_detail VARCHAR(1000) NULL COMMENT '소비 처리 결과 상세',
    received_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수신 일시',
    consumed_at DATETIME(3) NULL COMMENT '소비 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (inbox_id),
    UNIQUE KEY uk_cpf_broker_inbox_message (message_id),
    INDEX ix_cpf_broker_inbox_idempotency (idempotency_key),
    INDEX ix_cpf_broker_inbox_status (inbox_status, received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker Inbox';

CREATE TABLE IF NOT EXISTS cpf_broker_dlq (
    dlq_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'DLQ 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    topic VARCHAR(160) NOT NULL COMMENT 'Broker topic 또는 queue',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    failure_reason VARCHAR(1000) NULL COMMENT 'DLQ 이동 사유',
    replay_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '재처리 상태',
    replay_count INT NOT NULL DEFAULT 0 COMMENT '재처리 요청 횟수',
    replay_requested_at DATETIME(3) NULL COMMENT '재처리 요청 일시',
    replay_completed_at DATETIME(3) NULL COMMENT '재처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (dlq_id),
    UNIQUE KEY uk_cpf_broker_dlq_message (message_id),
    INDEX ix_cpf_broker_dlq_status (replay_status, created_at),
    INDEX ix_cpf_broker_dlq_topic (topic, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker DLQ';

CREATE TABLE IF NOT EXISTS cpf_file_transfer_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파일 전송 이력 내부 순번',
    transfer_id VARCHAR(260) NOT NULL COMMENT '파일 전송 ID',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '파일 전송 endpoint 코드',
    transfer_operation VARCHAR(30) NOT NULL COMMENT '전송 작업 유형',
    local_path VARCHAR(1000) NULL COMMENT '로컬 파일 경로',
    remote_path VARCHAR(1000) NULL COMMENT '원격 파일 경로',
    checksum VARCHAR(128) NULL COMMENT '파일 checksum',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '파일 크기',
    duplicate_key VARCHAR(1200) NOT NULL COMMENT '중복 방지 키',
    transfer_status VARCHAR(30) NOT NULL COMMENT '전송 상태',
    result_detail VARCHAR(1000) NULL COMMENT '전송 결과 상세',
    completed_at DATETIME(3) NULL COMMENT '전송 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (history_id),
    UNIQUE KEY uk_cpf_file_transfer_history_id (transfer_id),
    INDEX ix_cpf_file_transfer_duplicate (endpoint_code, duplicate_key(255), checksum),
    INDEX ix_cpf_file_transfer_tx (transaction_global_id, segment_id),
    INDEX ix_cpf_file_transfer_status (transfer_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 파일 전송 이력';

CREATE TABLE IF NOT EXISTS cpf_unknown_result (
    unknown_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Unknown result 내부 순번',
    unknown_id VARCHAR(120) NOT NULL COMMENT 'Unknown result ID',
    unknown_type VARCHAR(40) NOT NULL COMMENT 'Unknown result 유형',
    unknown_status VARCHAR(40) NOT NULL DEFAULT 'CHECK_PENDING' COMMENT 'Unknown result 상태',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    external_key VARCHAR(200) NULL COMMENT '외부 시스템 또는 메시지 키',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    next_action VARCHAR(100) NULL COMMENT '다음 조치',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '감지 일시',
    resolved_at DATETIME(3) NULL COMMENT '해결 일시',
    resolved_by VARCHAR(100) NULL COMMENT '해결 운영자',
    audit_reason VARCHAR(500) NULL COMMENT '수동 처리 감사 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (unknown_seq),
    UNIQUE KEY uk_cpf_unknown_result_id (unknown_id),
    INDEX ix_cpf_unknown_result_status (unknown_type, unknown_status, detected_at),
    INDEX ix_cpf_unknown_result_tx (transaction_global_id, segment_id),
    INDEX ix_cpf_unknown_result_external (external_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Unknown result 및 reconciliation 이력';
-- ============================================================================
-- specs/sql/20_cmn_schema.sql
-- ============================================================================
-- CMN 고객 업무 공통 Extension의 선택형 DB 검증 스키마입니다.
-- cpf-common은 기본적으로 DB 없이 동작하며, cmnDB에는 표준 DB 기능을 검증하는
-- cmn_sample_item 한 개만 둡니다. 업무 채번, 알림/업무 로그와 고정길이 전문
-- Runtime/Table은 CMN 기본 제품의 소유가 아닙니다.

USE cmnDB;

CREATE TABLE IF NOT EXISTS cmn_sample_item (
    sample_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '샘플 항목 ID',
    sample_key VARCHAR(100) NOT NULL COMMENT '외부 노출용 고유 샘플 키',
    item_name VARCHAR(200) NOT NULL COMMENT '샘플 항목명',
    category_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '검색 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    searchable_text VARCHAR(500) NULL COMMENT '검색 검증용 문자열',
    owner_reference VARCHAR(100) NULL COMMENT '다른 Domain을 직접 조인하지 않는 샘플 참조값',
    sort_order BIGINT NOT NULL DEFAULT 0 COMMENT '안정 정렬용 순번',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_cmn_sample_item_key UNIQUE (sample_key),
    CONSTRAINT ck_cmn_sample_item_status
        CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_cmn_sample_item_version
        CHECK (version_no >= 0),
    CONSTRAINT ck_cmn_sample_item_deleted
        CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_cmn_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_name_sort (item_name, sample_item_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='CMN DB 연결·CRUD·검색·Paging·낙관적 잠금 검증용 단일 샘플';
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
-- ============================================================================
-- specs/sql/35_bat_schema.sql
-- ============================================================================
-- BAT가 소유하는 Spring Batch 메타와 배치 런타임 스키마입니다.
-- 표준 Spring Batch 테이블은 BATCH_* 이름을 유지하고 BAT 전용 런타임 테이블은 bat_* 이름을 사용합니다.

USE batDB;

CREATE TABLE IF NOT EXISTS bat_on_demand_request (
    execution_request_id VARCHAR(36) NOT NULL COMMENT '온라인 접수 실행 요청 ID',
    standard_batch_id CHAR(10) NOT NULL COMMENT 'B 유형 10자리 표준 배치 ID',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 접수 방지 멱등 키',
    transaction_global_id VARCHAR(100) NOT NULL COMMENT '온라인 접수 거래 글로벌 ID',
    business_date CHAR(8) NOT NULL COMMENT '배치 업무 기준일 YYYYMMDD',
    request_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED, RUNNING, COMPLETED, FAILED, RESTARTED, STOPPING 등 접수 상태',
    parameters_json LONGTEXT NULL COMMENT '검증된 배치 업무 파라미터 JSON',
    request_reason VARCHAR(500) NOT NULL COMMENT '실행 감사 사유',
    request_user VARCHAR(100) NOT NULL COMMENT '실행 요청자',
    cpf_execution_id BIGINT NULL COMMENT 'BAT 배치 실행 메타 ID',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    result_json LONGTEXT NULL COMMENT '마스킹된 실행 결과 JSON',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '민감정보가 제거된 실패 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '접수일시',
    completed_at DATETIME(3) NULL COMMENT '완료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (execution_request_id),
    UNIQUE KEY uk_bat_on_demand_idempotency (standard_batch_id, idempotency_key),
    INDEX ix_bat_on_demand_status (request_status, requested_at),
    INDEX ix_bat_on_demand_transaction (transaction_global_id),
    INDEX ix_bat_on_demand_spring (spring_batch_execution_id),
    CONSTRAINT ck_bat_on_demand_id CHECK (
        standard_batch_id REGEXP '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
        AND RIGHT(standard_batch_id, 4) <> '0000'
    ),
    CONSTRAINT ck_bat_on_demand_status CHECK (
        request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED',
                           'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 온디맨드 배치 온라인 접수';

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

-- Spring Batch 5.2.4의 공식 MariaDB JobRepository 계약은 채번용 TABLE이 아니라
-- MariaDB SEQUENCE를 사용합니다. IF NOT EXISTS로 재설치 시 현재 next value를 보존합니다.
CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bat_job (
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_name VARCHAR(150) NOT NULL COMMENT '배치 Job 이름',
    job_type VARCHAR(30) NOT NULL DEFAULT 'TASKLET' COMMENT '배치 Job 유형',
    description VARCHAR(500) NULL COMMENT '배치 설명',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (job_id),
    INDEX ix_bat_job_use (use_yn, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Job 기준';

CREATE TABLE IF NOT EXISTS bat_schedule (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (schedule_id),
    INDEX ix_bat_schedule_job (job_id, enabled_yn),
    CONSTRAINT fk_bat_schedule_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 스케줄';

CREATE TABLE IF NOT EXISTS bat_job_relation (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 관계 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '기준 배치 Job ID',
    related_job_id VARCHAR(100) NOT NULL COMMENT '연관 배치 Job ID',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 유형',
    trigger_condition VARCHAR(50) NOT NULL DEFAULT 'COMPLETED' COMMENT '트리거 조건',
    required_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT '필수 선행 상태',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '관계 표시 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_bat_job_relation (job_id, related_job_id, relation_type),
    INDEX ix_bat_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_bat_job_relation_related (related_job_id, relation_type),
    CONSTRAINT fk_bat_job_relation_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_job_relation_related
        FOREIGN KEY (related_job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 선행/후행/트리거 관계';

CREATE TABLE IF NOT EXISTS bat_instance (
    instance_id VARCHAR(100) NOT NULL COMMENT '배치 인스턴스 ID',
    instance_name VARCHAR(150) NOT NULL COMMENT '배치 인스턴스명',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    server_port INT NULL COMMENT '서버 포트',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    description VARCHAR(500) NULL COMMENT '인스턴스 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (instance_id),
    INDEX ix_bat_instance_active (active_yn, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 서버 인스턴스';

CREATE TABLE IF NOT EXISTS bat_worker (
    worker_id VARCHAR(160) NOT NULL COMMENT '배치 worker ID',
    server_instance_id VARCHAR(160) NOT NULL COMMENT '서버 인스턴스 ID',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    process_id VARCHAR(80) NULL COMMENT '프로세스 ID',
    thread_name VARCHAR(160) NULL COMMENT '스레드명',
    worker_version VARCHAR(80) NOT NULL DEFAULT 'unknown' COMMENT 'worker 배포 버전',
    capabilities_json LONGTEXT NULL COMMENT 'worker 지원 Job 및 capability JSON',
    max_concurrency INT NOT NULL DEFAULT 1 COMMENT 'worker 최대 동시 실행 수',
    queue_capacity INT NOT NULL DEFAULT 1 COMMENT 'worker 내부 대기열 허용 수',
    control_status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING, DRAINING, STOPPED 제어 상태',
    worker_status VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT 'worker 상태',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    current_job_id VARCHAR(100) NULL COMMENT '현재 실행 Job ID',
    current_execution_id BIGINT NULL COMMENT '현재 BAT 배치 실행 순번',
    description VARCHAR(500) NULL COMMENT 'worker 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (worker_id),
    INDEX ix_bat_worker_server (server_instance_id, active_yn),
    INDEX ix_bat_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_bat_worker_control (control_status, active_yn, last_heartbeat_at),
    INDEX ix_bat_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker heartbeat';

CREATE TABLE IF NOT EXISTS bat_execution (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    job_parameters VARCHAR(2000) NULL COMMENT '배치 파라미터',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    spring_batch_job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID',
    business_date DATE NULL COMMENT 'JobInstance 시작 시 확정한 업무일자',
    run_id VARCHAR(120) NULL COMMENT '최초 실행 회차 ID',
    rerun_id VARCHAR(120) NULL COMMENT '운영 재수행 ID',
    original_job_execution_id BIGINT NULL COMMENT '재시작 기준 원 JobExecution ID',
    restart_attempt INT NOT NULL DEFAULT 0 COMMENT '동일 JobInstance 재시작 회차',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    required_worker_version VARCHAR(80) NULL COMMENT '실행에 필요한 worker 버전',
    required_capability VARCHAR(120) NULL COMMENT '실행에 필요한 worker capability',
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '온라인 호출 또는 상위 배치 거래 ID',
    transaction_segment_id VARCHAR(120) NULL COMMENT '배치 Job 거래 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID',
    job_log_relative_path VARCHAR(1000) NULL COMMENT 'CPF_LOG_ROOT 기준 JobInstance 로그 상대 경로',
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (execution_id),
    INDEX ix_bat_execution_job_time (job_id, start_time),
    INDEX ix_bat_execution_status (execution_status, start_time),
    INDEX ix_bat_execution_spring (spring_batch_execution_id),
    INDEX ix_bat_execution_job_instance (spring_batch_job_instance_id, business_date),
    INDEX ix_bat_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_bat_execution_claim (execution_status, required_worker_version, required_capability, execution_id),
    INDEX ix_bat_execution_transaction (transaction_global_id),
    INDEX ix_bat_execution_parent_transaction (parent_transaction_global_id),
    INDEX ix_bat_execution_segment (transaction_segment_id, parent_segment_id),
    INDEX ix_bat_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_bat_execution_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_execution_instance
        FOREIGN KEY (batch_instance_id) REFERENCES bat_instance(instance_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 실행 이력';

CREATE TABLE IF NOT EXISTS bat_execution_lease (
    lease_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 lease 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    worker_id VARCHAR(160) NOT NULL COMMENT '현재 lease 소유 worker ID',
    lease_token VARCHAR(80) NOT NULL COMMENT 'lease 갱신·완료 검증 토큰',
    lease_status VARCHAR(30) NOT NULL DEFAULT 'CLAIMED' COMMENT 'CLAIMED, RUNNING, RELEASED, EXPIRED 상태',
    claimed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 claim 일시',
    lease_until DATETIME(3) NOT NULL COMMENT 'lease 만료 일시',
    last_heartbeat_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '마지막 lease heartbeat 일시',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT 'claim 시도 회차',
    takeover_count INT NOT NULL DEFAULT 0 COMMENT '만료 후 다른 worker 인수 횟수',
    released_at DATETIME(3) NULL COMMENT '정상 또는 실패 완료 일시',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실행 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (lease_id),
    UNIQUE KEY uk_bat_execution_lease_execution (execution_id),
    UNIQUE KEY uk_bat_execution_lease_token (lease_token),
    INDEX ix_bat_execution_lease_owner (worker_id, lease_status, lease_until),
    INDEX ix_bat_execution_lease_expire (lease_status, lease_until),
    CONSTRAINT fk_bat_execution_lease_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_execution_lease_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker 실행 claim과 lease';

CREATE TABLE IF NOT EXISTS bat_execution_target (
    target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 수행 대상 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    target_instance_id VARCHAR(100) NULL COMMENT '수행 대상 인스턴스 ID',
    business_date DATE NULL COMMENT '업무 기준일',
    planned_run_at DATETIME(3) NULL COMMENT '예정 수행 일시',
    dispatch_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '배정 상태',
    dispatch_reason VARCHAR(500) NULL COMMENT '배정 또는 제외 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    INDEX ix_bat_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_bat_execution_target_execution (execution_id),
    INDEX ix_bat_execution_target_instance (target_instance_id, dispatch_status),
    CONSTRAINT fk_bat_execution_target_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_execution_target_schedule
        FOREIGN KEY (schedule_id) REFERENCES bat_schedule(schedule_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_instance
        FOREIGN KEY (target_instance_id) REFERENCES bat_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 수행 대상/대기 인스턴스';

CREATE TABLE IF NOT EXISTS bat_step_execution (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (step_execution_id),
    INDEX ix_bat_step_execution_parent (execution_id, step_name),
    INDEX ix_bat_step_execution_spring (spring_batch_step_execution_id),
    INDEX ix_bat_step_execution_worker (worker_id, start_time),
    INDEX ix_bat_step_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_bat_step_execution_parent
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_step_execution_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Step 실행 이력';

CREATE TABLE IF NOT EXISTS bat_lock (
    lock_key VARCHAR(200) NOT NULL COMMENT '배치 잠금 키',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_parameters_hash VARCHAR(128) NOT NULL COMMENT 'Job 파라미터 해시',
    owner_id VARCHAR(100) NOT NULL COMMENT '잠금 소유자',
    locked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '잠금 획득 일시',
    expire_at DATETIME(3) NOT NULL COMMENT '잠금 만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (lock_key),
    INDEX ix_bat_lock_job (job_id, job_parameters_hash),
    INDEX ix_bat_lock_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 중복 실행 방지 잠금';

CREATE TABLE IF NOT EXISTS bat_operation_log (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_job_time (job_id, created_at),
    INDEX ix_bat_operation_execution (execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 운영 작업 로그';

CREATE TABLE IF NOT EXISTS bat_ghost_event (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ghost_event_id),
    INDEX ix_bat_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_bat_ghost_event_job (job_id, detected_at),
    INDEX ix_bat_ghost_event_worker (worker_id, detected_at),
    CONSTRAINT fk_bat_ghost_event_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_ghost_event_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_ghost_event_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 ghost 감지와 조치 이력';


CREATE TABLE IF NOT EXISTS bat_business_day_calendar (
    calendar_id VARCHAR(50) NOT NULL COMMENT '캘린더 ID',
    business_date DATE NOT NULL COMMENT '기준 일자',
    holiday_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '휴일 여부',
    business_day_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '영업일 여부',
    description VARCHAR(500) NULL COMMENT '일자 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (calendar_id, business_date),
    INDEX ix_bat_business_day_calendar_date (business_date, business_day_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 영업일 캘린더';

CREATE TABLE IF NOT EXISTS bat_center_cut_job (
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    batch_job_id VARCHAR(100) NULL COMMENT '연결된 BAT 배치 Job ID',
    center_cut_job_name VARCHAR(150) NOT NULL COMMENT '센터컷 Job 명',
    provider_key VARCHAR(100) NOT NULL COMMENT '대상 조회 Provider 식별자',
    handler_key VARCHAR(100) NOT NULL COMMENT '처리 Handler 식별자',
    chunk_size INT NOT NULL DEFAULT 100 COMMENT '한 번에 조회할 대상 건수',
    retry_limit INT NOT NULL DEFAULT 3 COMMENT '최대 재처리 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    description VARCHAR(500) NULL COMMENT '센터컷 Job 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_job_id),
    INDEX ix_bat_center_cut_job_batch (batch_job_id, use_yn),
    CONSTRAINT fk_bat_center_cut_job_batch
        FOREIGN KEY (batch_job_id) REFERENCES bat_job(job_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 Job 정의';

CREATE TABLE IF NOT EXISTS bat_center_cut_parameter (
    parameter_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 파라미터 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    parameter_key VARCHAR(100) NOT NULL COMMENT '파라미터 키',
    parameter_value VARCHAR(1000) NULL COMMENT '파라미터 값',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (parameter_id),
    UNIQUE KEY uk_bat_center_cut_parameter (center_cut_job_id, parameter_key),
    CONSTRAINT fk_bat_center_cut_parameter_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 파라미터';

CREATE TABLE IF NOT EXISTS bat_center_cut_item (
    center_cut_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NULL COMMENT '업무 기준일',
    item_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    item_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    started_at DATETIME(3) NULL COMMENT '처리 시작 일시',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_item_id),
    UNIQUE KEY uk_bat_center_cut_item_business (center_cut_job_id, business_key),
    INDEX ix_bat_center_cut_item_status (center_cut_job_id, item_status, business_date),
    INDEX ix_bat_center_cut_item_transaction (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_bat_center_cut_item_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 대상';

CREATE TABLE IF NOT EXISTS bat_center_cut_result (
    center_cut_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 결과 순번',
    center_cut_item_id BIGINT NOT NULL COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_result_id),
    INDEX ix_bat_center_cut_result_item (center_cut_item_id, result_status),
    INDEX ix_bat_center_cut_result_job (center_cut_job_id, created_at),
    CONSTRAINT fk_bat_center_cut_result_item
        FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item(center_cut_item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_result_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 결과';
-- ============================================================================
-- specs/sql/40_business_modules_schema.sql
-- ============================================================================
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
-- ============================================================================
-- specs/sql/45_external_schema.sql
-- ============================================================================
-- EXS 대외연계 스키마입니다.
-- 기관·채널·endpoint 정책과 멱등 실행, 결과 불명 복구 원장을 EXS가 소유합니다.

USE exsDB;

CREATE TABLE IF NOT EXISTS exs_institution (
    institution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외기관 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    institution_name VARCHAR(120) NOT NULL COMMENT '대외기관명',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (institution_id),
    UNIQUE KEY uk_exs_institution_code (institution_code),
    CONSTRAINT ck_exs_institution_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외기관';

CREATE TABLE IF NOT EXISTS exs_channel (
    channel_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 채널 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    channel_code VARCHAR(50) NOT NULL COMMENT '대외 채널 코드',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '채널 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (channel_id),
    UNIQUE KEY uk_exs_channel_code (institution_code, channel_code),
    INDEX ix_exs_channel_enabled (enabled_yn, institution_code),
    CONSTRAINT ck_exs_channel_direction CHECK (direction IN ('SEND', 'RECEIVE', 'BIDIRECTIONAL')),
    CONSTRAINT ck_exs_channel_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 채널';

CREATE TABLE IF NOT EXISTS exs_endpoint (
    endpoint_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 endpoint 순번',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    service_id VARCHAR(100) NOT NULL COMMENT 'CPF 서비스 레지스트리 ID',
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    endpoint_uri VARCHAR(500) NOT NULL COMMENT '대외 endpoint 상대 URI',
    result_query_uri VARCHAR(500) NULL COMMENT '결과 불명 재조회 상대 URI',
    auth_profile_code VARCHAR(80) NULL COMMENT '인증 프로파일 코드',
    timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '호출 제한 시간 밀리초',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '멱등 호출 재시도 횟수',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'endpoint 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (endpoint_id),
    UNIQUE KEY uk_exs_endpoint_code (endpoint_code),
    INDEX ix_exs_endpoint_institution (institution_code, enabled_yn),
    CONSTRAINT ck_exs_endpoint_timeout CHECK (timeout_ms BETWEEN 1 AND 120000),
    CONSTRAINT ck_exs_endpoint_retry CHECK (retry_count BETWEEN 0 AND 10),
    CONSTRAINT ck_exs_endpoint_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 endpoint';

CREATE TABLE IF NOT EXISTS exs_auth_profile (
    auth_profile_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 인증 프로파일 순번',
    auth_profile_code VARCHAR(80) NOT NULL COMMENT '대외 인증 프로파일 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    auth_type VARCHAR(30) NOT NULL COMMENT '인증 유형',
    secret_ref VARCHAR(300) NULL COMMENT '외부 secret 참조 경로',
    certificate_ref VARCHAR(300) NULL COMMENT '외부 인증서 참조 경로',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '인증 프로파일 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (auth_profile_id),
    UNIQUE KEY uk_exs_auth_profile_code (auth_profile_code),
    INDEX ix_exs_auth_profile_institution (institution_code, enabled_yn),
    CONSTRAINT ck_exs_auth_profile_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 인증 프로파일';

CREATE TABLE IF NOT EXISTS exs_control_policy (
    control_policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 통제 정책 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    control_type VARCHAR(30) NOT NULL COMMENT '통제 유형',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    reason VARCHAR(500) NULL COMMENT '통제 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (control_policy_id),
    UNIQUE KEY uk_exs_control_policy (institution_code, control_type),
    INDEX ix_exs_control_policy_enabled (enabled_yn, institution_code),
    CONSTRAINT ck_exs_control_policy_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 기관별 송수신 통제 정책';

CREATE TABLE IF NOT EXISTS exs_execution (
    execution_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 실행 순번',
    execution_id VARCHAR(80) NOT NULL COMMENT '대외 실행 ID',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    external_request_id VARCHAR(120) NOT NULL COMMENT '기관 요청 ID',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '멱등 요청 키',
    request_hash CHAR(64) NOT NULL COMMENT '정규화 요청 SHA-256',
    execution_status VARCHAR(30) NOT NULL COMMENT '실행 상태',
    response_json LONGTEXT NULL COMMENT '마스킹된 응답 JSON',
    unknown_result_id VARCHAR(80) NULL COMMENT 'CPF 결과 불명 원장 ID',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    recovery_operator_id VARCHAR(100) NULL COMMENT '복구 작업자 ID',
    recovery_reason VARCHAR(1000) NULL COMMENT '복구 감사 사유',
    recovered_at DATETIME(3) NULL COMMENT '복구 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (execution_seq),
    UNIQUE KEY uk_exs_execution_id (execution_id),
    UNIQUE KEY uk_exs_execution_idempotency (idempotency_key),
    UNIQUE KEY uk_exs_execution_external_request (institution_code, external_request_id),
    INDEX ix_exs_execution_status_time (execution_status, created_at),
    INDEX ix_exs_execution_unknown (unknown_result_id),
    CONSTRAINT ck_exs_execution_status CHECK (execution_status IN ('REQUESTED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 멱등 대외 실행';

CREATE TABLE IF NOT EXISTS exs_reconciliation_log (
    reconciliation_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '정합성 확인 로그 순번',
    execution_id VARCHAR(80) NOT NULL COMMENT '대외 실행 ID',
    unknown_result_id VARCHAR(80) NOT NULL COMMENT 'CPF 결과 불명 원장 ID',
    before_status VARCHAR(30) NOT NULL COMMENT '변경 전 상태',
    after_status VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    operator_id VARCHAR(100) NOT NULL COMMENT '복구 작업자 ID',
    audit_reason VARCHAR(1000) NOT NULL COMMENT '복구 감사 사유',
    source_type VARCHAR(30) NOT NULL COMMENT '기관 조회 또는 수동 확정 구분',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (reconciliation_log_id),
    INDEX ix_exs_reconciliation_execution (execution_id, created_at),
    INDEX ix_exs_reconciliation_unknown (unknown_result_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 결과 불명 정합성 확인 이력';
