-- 독립 BAT worker의 등록, capability 매칭, claim, lease, takeover 제어 메타를 추가합니다.
USE pfwDB;

ALTER TABLE pfw_batch_worker
    ADD COLUMN IF NOT EXISTS worker_version VARCHAR(80) NOT NULL DEFAULT 'unknown' COMMENT 'worker 배포 버전' AFTER thread_name,
    ADD COLUMN IF NOT EXISTS capabilities_json LONGTEXT NULL COMMENT 'worker 지원 Job 및 capability JSON' AFTER worker_version,
    ADD COLUMN IF NOT EXISTS max_concurrency INT NOT NULL DEFAULT 1 COMMENT 'worker 최대 동시 실행 수' AFTER capabilities_json,
    ADD COLUMN IF NOT EXISTS queue_capacity INT NOT NULL DEFAULT 1 COMMENT 'worker 내부 대기열 허용 수' AFTER max_concurrency,
    ADD COLUMN IF NOT EXISTS control_status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING, DRAINING, STOPPED 제어 상태' AFTER queue_capacity;

CREATE INDEX IF NOT EXISTS ix_pfw_batch_worker_control
    ON pfw_batch_worker (control_status, active_yn, last_heartbeat_at);

ALTER TABLE pfw_batch_execution
    ADD COLUMN IF NOT EXISTS required_worker_version VARCHAR(80) NULL COMMENT '실행에 필요한 worker 버전' AFTER worker_id,
    ADD COLUMN IF NOT EXISTS required_capability VARCHAR(120) NULL COMMENT '실행에 필요한 worker capability' AFTER required_worker_version;

CREATE INDEX IF NOT EXISTS ix_pfw_batch_execution_claim
    ON pfw_batch_execution (execution_status, required_worker_version, required_capability, execution_id);

CREATE TABLE IF NOT EXISTS pfw_batch_execution_lease (
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
    UNIQUE KEY uk_pfw_batch_execution_lease_execution (execution_id),
    UNIQUE KEY uk_pfw_batch_execution_lease_token (lease_token),
    INDEX ix_pfw_batch_execution_lease_owner (worker_id, lease_status, lease_until),
    INDEX ix_pfw_batch_execution_lease_expire (lease_status, lease_until),
    CONSTRAINT fk_pfw_batch_execution_lease_execution
        FOREIGN KEY (execution_id) REFERENCES pfw_batch_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pfw_batch_execution_lease_worker
        FOREIGN KEY (worker_id) REFERENCES pfw_batch_worker(worker_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 배치 worker 실행 claim과 lease';
