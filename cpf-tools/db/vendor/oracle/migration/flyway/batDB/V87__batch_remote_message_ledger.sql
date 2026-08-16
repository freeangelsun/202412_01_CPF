CREATE TABLE bat_remote_message_ledger (
    direction_cd VARCHAR2(20 CHAR) NOT NULL,
    message_id VARCHAR2(64 CHAR) NOT NULL,
    payload_sha256 VARCHAR2(64 CHAR) NOT NULL,
    status_cd VARCHAR2(20 CHAR) NOT NULL,
    owner_id VARCHAR2(150 CHAR) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    attempt_no NUMBER(10) DEFAULT 1 NOT NULL,
    last_error_cd VARCHAR2(100 CHAR),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no NUMBER(19) DEFAULT 1 NOT NULL,
    CONSTRAINT pk_bat_remote_message_ledger PRIMARY KEY (direction_cd, message_id),
    CONSTRAINT ck_bat_remote_msg_attempt CHECK (attempt_no > 0),
    CONSTRAINT ck_bat_remote_msg_version CHECK (version_no > 0)
);
CREATE INDEX idx_bat_remote_msg_status ON bat_remote_message_ledger (status_cd, lease_until);
CREATE INDEX idx_bat_remote_msg_expiry ON bat_remote_message_ledger (expires_at);
