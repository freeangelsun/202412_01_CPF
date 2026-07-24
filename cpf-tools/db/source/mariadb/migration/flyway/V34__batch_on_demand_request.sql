-- BAT 온디맨드 202 접수와 Spring Batch 실행 연결 상태를 저장합니다.
CREATE TABLE IF NOT EXISTS cpf_batch_on_demand_request (
    execution_request_id VARCHAR(36) NOT NULL COMMENT '온라인 접수 실행 요청 ID',
    standard_batch_id CHAR(10) NOT NULL COMMENT 'B 유형 10자리 표준 배치 ID',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 접수 방지 멱등 키',
    transaction_global_id VARCHAR(100) NOT NULL COMMENT '온라인 접수 거래 글로벌 ID',
    business_date CHAR(8) NOT NULL COMMENT '배치 업무 기준일 YYYYMMDD',
    request_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED, RUNNING, COMPLETED, FAILED, RESTARTED, STOPPING 등 접수 상태',
    parameters_json LONGTEXT NULL COMMENT '검증된 배치 업무 파라미터 JSON',
    request_reason VARCHAR(500) NOT NULL COMMENT '실행 감사 사유',
    request_user VARCHAR(100) NOT NULL COMMENT '실행 요청자',
    cpf_execution_id BIGINT NULL COMMENT 'CPF 배치 실행 메타 ID',
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
    UNIQUE KEY uk_cpf_batch_on_demand_idempotency (standard_batch_id, idempotency_key),
    INDEX ix_cpf_batch_on_demand_status (request_status, requested_at),
    INDEX ix_cpf_batch_on_demand_transaction (transaction_global_id),
    INDEX ix_cpf_batch_on_demand_spring (spring_batch_execution_id),
    CONSTRAINT ck_cpf_batch_on_demand_id CHECK (
        standard_batch_id REGEXP '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
        AND RIGHT(standard_batch_id, 4) <> '0000'
    ),
    CONSTRAINT ck_cpf_batch_on_demand_status CHECK (
        request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED',
                           'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 온디맨드 배치 온라인 접수';
