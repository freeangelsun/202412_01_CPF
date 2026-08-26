-- CPF R140: recovery-only recreation of the retired ledger for rollback compatibility.
CREATE TABLE BAT_REMOTE_MESSAGE_LEDGER (
    direction_cd VARCHAR(20) NOT NULL,
    message_id VARCHAR(64) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    status_cd VARCHAR(20) NOT NULL,
    owner_id VARCHAR(150) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    attempt_no INTEGER NOT NULL DEFAULT 1,
    last_error_cd VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_BAT_REMOTE_MESSAGE_LEDGER PRIMARY KEY (direction_cd, message_id),
    CONSTRAINT ck_bat_remote_msg_attempt CHECK (attempt_no > 0),
    CONSTRAINT ck_bat_remote_msg_version CHECK (version_no > 0)
);
CREATE INDEX idx_bat_remote_msg_status ON BAT_REMOTE_MESSAGE_LEDGER (status_cd, lease_until);
CREATE INDEX idx_bat_remote_msg_expiry ON BAT_REMOTE_MESSAGE_LEDGER (expires_at);
COMMENT ON TABLE BAT_REMOTE_MESSAGE_LEDGER IS 'Kafka Remote Batch at-least-once Idempotency/Fencing Ledger';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.direction_cd IS 'REQUEST 또는 REPLY';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.message_id IS '안정 Remote Message 식별자';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.payload_sha256 IS 'Payload SHA-256';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.status_cd IS 'PROCESSING COMPLETE FAILED';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.owner_id IS '현재 처리 인스턴스';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.lease_until IS '처리 Lease 만료 일시';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.expires_at IS 'Message TTL 만료 일시';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.attempt_no IS '처리 시도 횟수';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.last_error_cd IS '마지막 Sanitized 오류 코드';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.created_at IS '최초 수신 일시';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.updated_at IS '최종 상태 변경 일시';
COMMENT ON COLUMN BAT_REMOTE_MESSAGE_LEDGER.version_no IS 'Fencing/낙관적 잠금 버전';
