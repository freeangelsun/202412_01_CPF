-- CPF R140: recovery-only recreation of the retired ledger for rollback compatibility.
CREATE TABLE IF NOT EXISTS BAT_REMOTE_MESSAGE_LEDGER (
    direction_cd VARCHAR(20) NOT NULL COMMENT 'REQUEST 또는 REPLY',
    message_id VARCHAR(64) NOT NULL COMMENT '안정 Remote Message 식별자',
    payload_sha256 VARCHAR(64) NOT NULL COMMENT 'Payload SHA-256',
    status_cd VARCHAR(20) NOT NULL COMMENT 'PROCESSING COMPLETE FAILED',
    owner_id VARCHAR(150) NOT NULL COMMENT '현재 처리 인스턴스',
    lease_until DATETIME(6) NOT NULL COMMENT '처리 Lease 만료 일시',
    expires_at DATETIME(6) NOT NULL COMMENT 'Message TTL 만료 일시',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT '처리 시도 횟수',
    last_error_cd VARCHAR(100) NULL DEFAULT NULL COMMENT '마지막 Sanitized 오류 코드',
    created_at DATETIME(6) NOT NULL COMMENT '최초 수신 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 상태 변경 일시',
    version_no BIGINT NOT NULL DEFAULT 1 COMMENT 'Fencing/낙관적 잠금 버전',
    CONSTRAINT pk_BAT_REMOTE_MESSAGE_LEDGER PRIMARY KEY (direction_cd, message_id),
    CONSTRAINT ck_bat_remote_msg_attempt CHECK (attempt_no > 0),
    CONSTRAINT ck_bat_remote_msg_version CHECK (version_no > 0),
    INDEX idx_bat_remote_msg_status (status_cd, lease_until),
    INDEX idx_bat_remote_msg_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Kafka Remote Batch at-least-once Idempotency/Fencing Ledger';
