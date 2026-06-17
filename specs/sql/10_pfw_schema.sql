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

CREATE TABLE IF NOT EXISTS pfw_batch_execution (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    job_parameters VARCHAR(2000) NULL COMMENT '배치 파라미터',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
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
    CONSTRAINT fk_pfw_batch_execution_job
        FOREIGN KEY (job_id) REFERENCES pfw_batch_job(job_id),
    CONSTRAINT fk_pfw_batch_execution_instance
        FOREIGN KEY (batch_instance_id) REFERENCES pfw_batch_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 실행 이력';

CREATE TABLE IF NOT EXISTS pfw_batch_step_execution (
    step_execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 Step 실행 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    step_name VARCHAR(150) NOT NULL COMMENT 'Step 이름',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    step_log MEDIUMTEXT NULL COMMENT 'Step 로그',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (step_execution_id),
    INDEX ix_pfw_batch_step_execution_parent (execution_id, step_name),
    CONSTRAINT fk_pfw_batch_step_execution_parent
        FOREIGN KEY (execution_id) REFERENCES pfw_batch_execution(execution_id)
        ON DELETE CASCADE
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
