-- PFW 프레임워크 엔진 스키마입니다.
-- 거래로그, 시스템 코드/메시지, 응답코드, 설정, 캐시 이벤트, 보안 메타, 배치 운영 메타를 pfwDB에 배치합니다.

USE pfwDB;

CREATE TABLE IF NOT EXISTS pfw_transaction_log (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (LOG_IDX),
    UNIQUE KEY uk_pfw_transaction_log_recovery_event (RECOVERY_EVENT_ID),
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

CREATE TABLE IF NOT EXISTS pfw_transaction_segment (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (segment_id),
    UNIQUE KEY uk_pfw_transaction_segment_id (transaction_segment_id),
    INDEX ix_pfw_transaction_segment_global (transaction_global_id, started_at, segment_id),
    INDEX ix_pfw_transaction_segment_parent (parent_segment_id),
    INDEX ix_pfw_transaction_segment_module (module_code, started_at),
    INDEX ix_pfw_transaction_segment_role (transaction_role, direction),
    INDEX ix_pfw_transaction_segment_status (failure_yn, status, started_at),
    INDEX ix_pfw_transaction_segment_duration (duration_ms),
    INDEX ix_pfw_transaction_segment_customer (customer_no_masked, started_at),
    INDEX ix_pfw_transaction_segment_member (member_no_masked, started_at),
    INDEX ix_pfw_transaction_segment_user (user_id_masked, started_at),
    INDEX ix_pfw_transaction_segment_operator (operator_id_masked, started_at),
    INDEX ix_pfw_transaction_segment_client (client_app_id, caller_service, started_at),
    INDEX ix_pfw_transaction_segment_external (external_institution_code, external_transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 복합 거래 구간 로그';

CREATE TABLE IF NOT EXISTS pfw_service (
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    service_name VARCHAR(150) NOT NULL COMMENT '서비스명',
    service_type VARCHAR(30) NOT NULL DEFAULT 'INTERNAL' COMMENT '서비스 유형',
    owner_module_code VARCHAR(20) NOT NULL COMMENT '소유 모듈 코드',
    description VARCHAR(500) NULL COMMENT '서비스 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (service_id),
    INDEX ix_pfw_service_owner (owner_module_code, use_yn),
    INDEX ix_pfw_service_type (service_type, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 레지스트리';

CREATE TABLE IF NOT EXISTS pfw_service_endpoint (
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_name VARCHAR(150) NOT NULL COMMENT 'Endpoint명',
    endpoint_type VARCHAR(30) NOT NULL DEFAULT 'HTTP' COMMENT 'Endpoint 유형',
    base_url VARCHAR(500) NOT NULL COMMENT '기본 URL',
    context_path VARCHAR(200) NULL COMMENT 'Context path',
    default_timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '기본 timeout 밀리초',
    default_retry_count INT NOT NULL DEFAULT 0 COMMENT '기본 retry 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (endpoint_code),
    INDEX ix_pfw_service_endpoint_service (service_id, use_yn),
    INDEX ix_pfw_service_endpoint_type (endpoint_type, use_yn),
    CONSTRAINT fk_pfw_service_endpoint_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 Endpoint 레지스트리';

CREATE TABLE IF NOT EXISTS pfw_service_instance (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (instance_id),
    INDEX ix_pfw_service_instance_endpoint (service_id, endpoint_code, active_yn, instance_status),
    INDEX ix_pfw_service_instance_weight (endpoint_code, weight),
    CONSTRAINT fk_pfw_service_instance_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id),
    CONSTRAINT fk_pfw_service_instance_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES pfw_service_endpoint(endpoint_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 인스턴스 레지스트리';

CREATE TABLE IF NOT EXISTS pfw_service_health_status (
    health_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 health 이력 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    health_status VARCHAR(30) NOT NULL COMMENT 'Health 상태',
    http_status INT NULL COMMENT 'HTTP 상태 코드',
    response_time_ms BIGINT NULL COMMENT '응답 시간 밀리초',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    checked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '점검 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (health_id),
    INDEX ix_pfw_service_health_target (service_id, endpoint_code, instance_id, checked_at),
    INDEX ix_pfw_service_health_status (health_status, checked_at),
    CONSTRAINT fk_pfw_service_health_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id),
    CONSTRAINT fk_pfw_service_health_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES pfw_service_endpoint(endpoint_code),
    CONSTRAINT fk_pfw_service_health_instance
        FOREIGN KEY (instance_id) REFERENCES pfw_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 Health 상태 이력';

CREATE TABLE IF NOT EXISTS pfw_service_routing_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '라우팅 정책 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    routing_mode VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT '라우팅 모드',
    load_balance_type VARCHAR(30) NOT NULL DEFAULT 'WEIGHT' COMMENT '부하 분산 유형',
    failover_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Failover 사용 여부',
    health_check_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Health check 필수 여부',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    priority INT NOT NULL DEFAULT 100 COMMENT '우선순위',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_pfw_service_routing_policy (service_id, endpoint_code, priority),
    INDEX ix_pfw_service_routing_active (service_id, endpoint_code, active_yn, priority),
    CONSTRAINT fk_pfw_service_routing_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id),
    CONSTRAINT fk_pfw_service_routing_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES pfw_service_endpoint(endpoint_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 라우팅 정책';

CREATE TABLE IF NOT EXISTS pfw_service_circuit_state (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (circuit_id),
    UNIQUE KEY uk_pfw_service_circuit_state (service_id, endpoint_code, instance_id),
    INDEX ix_pfw_service_circuit_state (circuit_state, updated_at),
    CONSTRAINT fk_pfw_service_circuit_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id),
    CONSTRAINT fk_pfw_service_circuit_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES pfw_service_endpoint(endpoint_code),
    CONSTRAINT fk_pfw_service_circuit_instance
        FOREIGN KEY (instance_id) REFERENCES pfw_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 Circuit 상태';

CREATE TABLE IF NOT EXISTS pfw_service_call_history (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (call_id),
    INDEX ix_pfw_service_call_history_tx (transaction_global_id, call_id),
    INDEX ix_pfw_service_call_history_service (service_id, endpoint_code, created_at),
    INDEX ix_pfw_service_call_history_status (call_status, created_at),
    CONSTRAINT fk_pfw_service_call_history_service
        FOREIGN KEY (service_id) REFERENCES pfw_service(service_id),
    CONSTRAINT fk_pfw_service_call_history_endpoint
        FOREIGN KEY (endpoint_code) REFERENCES pfw_service_endpoint(endpoint_code)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_service_call_history_instance
        FOREIGN KEY (instance_id) REFERENCES pfw_service_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 서비스 호출 이력';

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
    spring_batch_job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID',
    business_date DATE NULL COMMENT 'JobInstance 시작 시 확정한 업무일자',
    run_id VARCHAR(120) NULL COMMENT '최초 실행 회차 ID',
    rerun_id VARCHAR(120) NULL COMMENT '운영 재수행 ID',
    original_job_execution_id BIGINT NULL COMMENT '재시작 기준 원 JobExecution ID',
    restart_attempt INT NOT NULL DEFAULT 0 COMMENT '동일 JobInstance 재시작 회차',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (execution_id),
    INDEX ix_pfw_batch_execution_job_time (job_id, start_time),
    INDEX ix_pfw_batch_execution_status (execution_status, start_time),
    INDEX ix_pfw_batch_execution_spring (spring_batch_execution_id),
    INDEX ix_pfw_batch_execution_job_instance (spring_batch_job_instance_id, business_date),
    INDEX ix_pfw_batch_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_pfw_batch_execution_transaction (transaction_global_id),
    INDEX ix_pfw_batch_execution_parent_transaction (parent_transaction_global_id),
    INDEX ix_pfw_batch_execution_segment (transaction_segment_id, parent_segment_id),
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

CREATE TABLE IF NOT EXISTS pfw_idempotency_record (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (idempotency_seq),
    UNIQUE KEY uk_pfw_idempotency_record_key (scope, idempotency_key),
    INDEX ix_pfw_idempotency_record_status (record_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 중복 처리 기록';

CREATE TABLE IF NOT EXISTS pfw_broker_outbox (
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
    broker_name VARCHAR(80) NULL COMMENT '전송 대상 broker 이름',
    partition_key VARCHAR(200) NULL COMMENT '전송 partition key',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '업무 이벤트 발생 일시',
    claimed_at DATETIME(3) NULL COMMENT 'worker 점유 일시',
    published_at DATETIME(3) NULL COMMENT '발행 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (outbox_id),
    UNIQUE KEY uk_pfw_broker_outbox_message (message_id),
    INDEX ix_pfw_broker_outbox_status (outbox_status, outbox_id),
    INDEX ix_pfw_broker_outbox_tx (transaction_global_id, segment_id),
    INDEX ix_pfw_broker_outbox_topic (topic, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW Broker Outbox';

CREATE TABLE IF NOT EXISTS pfw_broker_inbox (
    inbox_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Inbox 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    idempotency_key VARCHAR(160) NULL COMMENT '중복 처리 키',
    inbox_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED' COMMENT 'Inbox 처리 상태',
    result_detail VARCHAR(1000) NULL COMMENT '소비 처리 결과 상세',
    received_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수신 일시',
    consumed_at DATETIME(3) NULL COMMENT '소비 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (inbox_id),
    UNIQUE KEY uk_pfw_broker_inbox_message (message_id),
    INDEX ix_pfw_broker_inbox_idempotency (idempotency_key),
    INDEX ix_pfw_broker_inbox_status (inbox_status, received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW Broker Inbox';

CREATE TABLE IF NOT EXISTS pfw_broker_dlq (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (dlq_id),
    UNIQUE KEY uk_pfw_broker_dlq_message (message_id),
    INDEX ix_pfw_broker_dlq_status (replay_status, created_at),
    INDEX ix_pfw_broker_dlq_topic (topic, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW Broker DLQ';

CREATE TABLE IF NOT EXISTS pfw_file_transfer_history (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (history_id),
    UNIQUE KEY uk_pfw_file_transfer_history_id (transfer_id),
    INDEX ix_pfw_file_transfer_duplicate (endpoint_code, duplicate_key(255), checksum),
    INDEX ix_pfw_file_transfer_tx (transaction_global_id, segment_id),
    INDEX ix_pfw_file_transfer_status (transfer_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 파일 전송 이력';

CREATE TABLE IF NOT EXISTS pfw_unknown_result (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (unknown_seq),
    UNIQUE KEY uk_pfw_unknown_result_id (unknown_id),
    INDEX ix_pfw_unknown_result_status (unknown_type, unknown_status, detected_at),
    INDEX ix_pfw_unknown_result_tx (transaction_global_id, segment_id),
    INDEX ix_pfw_unknown_result_external (external_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW Unknown result 및 reconciliation 이력';
