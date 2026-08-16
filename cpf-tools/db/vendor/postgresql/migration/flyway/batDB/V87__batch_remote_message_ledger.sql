CREATE TABLE bat_remote_message_ledger (
    direction_cd VARCHAR(20) NOT NULL,
    message_id VARCHAR(64) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    status_cd VARCHAR(20) NOT NULL,
    owner_id VARCHAR(150) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    attempt_no INTEGER NOT NULL DEFAULT 1,
    last_error_cd VARCHAR(100),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_bat_remote_message_ledger PRIMARY KEY (direction_cd, message_id),
    CONSTRAINT ck_bat_remote_msg_attempt CHECK (attempt_no > 0),
    CONSTRAINT ck_bat_remote_msg_version CHECK (version_no > 0)
);
CREATE INDEX idx_bat_remote_msg_status ON bat_remote_message_ledger (status_cd, lease_until);
CREATE INDEX idx_bat_remote_msg_expiry ON bat_remote_message_ledger (expires_at);
