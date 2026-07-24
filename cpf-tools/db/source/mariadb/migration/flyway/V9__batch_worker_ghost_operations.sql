-- 배치 worker heartbeat와 ghost 조치 운영 메타를 추가합니다.
-- 기존 DB에 적용되는 증분 migration이며 신규 설치 기준은 V1 baseline에 동일 구조가 포함됩니다.

USE cpfDB;

CREATE TABLE IF NOT EXISTS cpf_batch_worker (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (worker_id),
    INDEX ix_cpf_batch_worker_server (server_instance_id, active_yn),
    INDEX ix_cpf_batch_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_cpf_batch_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 배치 worker heartbeat';

ALTER TABLE cpf_batch_execution
    ADD COLUMN IF NOT EXISTS server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID' AFTER batch_instance_id,
    ADD COLUMN IF NOT EXISTS worker_id VARCHAR(160) NULL COMMENT '실행 worker ID' AFTER server_instance_id,
    ADD COLUMN IF NOT EXISTS transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID' AFTER worker_id;

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_worker
    ON cpf_batch_execution (worker_id, execution_status, start_time);

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_transaction
    ON cpf_batch_execution (transaction_global_id);

ALTER TABLE cpf_batch_step_execution
    ADD COLUMN IF NOT EXISTS spring_batch_step_execution_id BIGINT NULL COMMENT 'Spring Batch StepExecution ID' AFTER execution_id,
    ADD COLUMN IF NOT EXISTS worker_id VARCHAR(160) NULL COMMENT '실행 worker ID' AFTER spring_batch_step_execution_id;

CREATE INDEX IF NOT EXISTS ix_cpf_batch_step_execution_spring
    ON cpf_batch_step_execution (spring_batch_step_execution_id);

CREATE INDEX IF NOT EXISTS ix_cpf_batch_step_execution_worker
    ON cpf_batch_step_execution (worker_id, start_time);

CREATE TABLE IF NOT EXISTS cpf_batch_ghost_event (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (ghost_event_id),
    INDEX ix_cpf_batch_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_cpf_batch_ghost_event_job (job_id, detected_at),
    INDEX ix_cpf_batch_ghost_event_worker (worker_id, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 배치 ghost 감지와 조치 이력';

INSERT INTO cpf_batch_worker (
    worker_id, server_instance_id, host_name, process_id, thread_name, worker_status,
    active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    'local-batch-01',
    'localhost',
    'migration',
    'migration-main',
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
