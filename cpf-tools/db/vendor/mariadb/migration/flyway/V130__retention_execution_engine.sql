-- CPF DB V130: executable retention engine policy/run state
-- Generated from canonical platform-schema.json; app runtime owns execution.

CREATE TABLE OPS_RETENTION_POLICY (
    policy_id VARCHAR(80) NOT NULL,
    target_name VARCHAR(80) NOT NULL,
    action_name VARCHAR(16) DEFAULT 'KEEP' NOT NULL,
    retention_days INT DEFAULT 90 NOT NULL,
    schedule_expression VARCHAR(100) NULL,
    maintenance_start VARCHAR(8) NULL,
    maintenance_end VARCHAR(8) NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    paused_yn CHAR(1) DEFAULT 'N' NOT NULL,
    legal_hold_yn CHAR(1) DEFAULT 'N' NOT NULL,
    chunk_size INT DEFAULT 1000 NOT NULL,
    throttle_millis BIGINT DEFAULT 0 NOT NULL,
    max_rows_per_run BIGINT DEFAULT 100000 NOT NULL,
    max_runtime_seconds BIGINT DEFAULT 300 NOT NULL,
    lease_seconds INT DEFAULT 60 NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    next_run_at DATETIME(3) NULL,
    last_run_at DATETIME(3) NULL,
    lease_owner VARCHAR(128) NULL,
    lease_until DATETIME(3) NULL,
    fencing_token BIGINT DEFAULT 0 NOT NULL,
    row_version BIGINT DEFAULT 0 NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_RETENTION_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT ck_ops_retention_policy_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_paused CHECK (paused_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_hold CHECK (legal_hold_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_action CHECK (action_name IN ('KEEP','ARCHIVE','PURGE')),
    CONSTRAINT ck_ops_retention_policy_chunk CHECK (chunk_size >= 1 AND chunk_size <= 100000),
    CONSTRAINT ck_ops_retention_policy_limits CHECK (max_rows_per_run >= 1 AND max_runtime_seconds >= 1 AND lease_seconds >= 5)
) ENGINE=InnoDB;
ALTER TABLE OPS_RETENTION_POLICY COMMENT = 'Shared retention policy, schedule and single-executor lease state';
CREATE INDEX ix_ops_retention_policy_due ON OPS_RETENTION_POLICY (enabled_yn, paused_yn, next_run_at);
CREATE INDEX ix_ops_retention_policy_lease ON OPS_RETENTION_POLICY (lease_until, lease_owner);
CREATE INDEX ix_ops_retention_policy_target ON OPS_RETENTION_POLICY (target_name, action_name);

CREATE TABLE OPS_RETENTION_RUN (
    run_id VARCHAR(64) NOT NULL,
    policy_id VARCHAR(80) NOT NULL,
    trigger_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) DEFAULT 'RUNNING' NOT NULL,
    runtime_instance_id VARCHAR(128) NOT NULL,
    actor_id VARCHAR(100) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    policy_version BIGINT NOT NULL,
    cutoff_at DATETIME(3) NULL,
    started_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    completed_at DATETIME(3) NULL,
    matched_count BIGINT DEFAULT 0 NOT NULL,
    archived_count BIGINT DEFAULT 0 NOT NULL,
    deleted_count BIGINT DEFAULT 0 NOT NULL,
    processed_count BIGINT DEFAULT 0 NOT NULL,
    compressed_count BIGINT DEFAULT 0 NOT NULL,
    freed_bytes BIGINT DEFAULT 0 NOT NULL,
    pause_requested_yn CHAR(1) DEFAULT 'N' NOT NULL,
    error_code VARCHAR(100) NULL,
    error_summary VARCHAR(500) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_RETENTION_RUN PRIMARY KEY (run_id),
    CONSTRAINT ck_ops_retention_run_trigger CHECK (trigger_type IN ('SCHEDULED','MANUAL','RESUME')),
    CONSTRAINT ck_ops_retention_run_status CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','PAUSED','SKIPPED','FAILED')),
    CONSTRAINT ck_ops_retention_run_pause CHECK (pause_requested_yn IN ('Y','N')),
    CONSTRAINT fk_ops_retention_run_policy FOREIGN KEY (policy_id) REFERENCES OPS_RETENTION_POLICY (policy_id)
) ENGINE=InnoDB;
ALTER TABLE OPS_RETENTION_RUN COMMENT = 'Retention run history and actual execution result';
CREATE INDEX ix_ops_retention_run_policy_time ON OPS_RETENTION_RUN (policy_id, started_at);
CREATE INDEX ix_ops_retention_run_status_time ON OPS_RETENTION_RUN (status, started_at);
CREATE INDEX ix_ops_retention_run_runtime ON OPS_RETENTION_RUN (runtime_instance_id, started_at);
