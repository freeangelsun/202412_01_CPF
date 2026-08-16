CREATE TABLE IF NOT EXISTS bat_execution_attempt (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT,
    execution_id BIGINT NOT NULL,
    attempt_no INT NOT NULL,
    definition_version BIGINT NOT NULL,
    definition_checksum VARCHAR(128) NOT NULL,
    worker_id VARCHAR(160) NOT NULL,
    fencing_token BIGINT NOT NULL,
    attempt_status VARCHAR(40) NOT NULL DEFAULT 'RUNNING',
    result_message MEDIUMTEXT NULL,
    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    finished_at DATETIME(3) NULL,
    CONSTRAINT pk_bat_execution_attempt PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_execution_attempt UNIQUE (execution_id, attempt_no),
    CONSTRAINT ck_bat_execution_attempt_status CHECK (attempt_status IN ('RUNNING','COMPLETED','FAILED','TIMEOUT','RETRYABLE_FAILURE','UNKNOWN_RESULT')),
    CONSTRAINT fk_bat_execution_attempt_execution FOREIGN KEY (execution_id)
        REFERENCES bat_execution (execution_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_execution_attempt_status ON bat_execution_attempt (attempt_status, started_at);
CREATE INDEX ix_bat_execution_attempt_worker ON bat_execution_attempt (worker_id, started_at);
