-- CPF V21 migration.
-- PFW 서비스 호출 엔진의 service/endpoint/instance/health/routing/circuit/call history 레지스트리를 추가합니다.

USE pfwDB;

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
