-- CPF DB V130: executable retention engine policy/run state
-- Generated from canonical platform-schema.json; app runtime owns execution.

CREATE TABLE OPS_RETENTION_POLICY (
    policy_id VARCHAR(80) NOT NULL,
    target_name VARCHAR(80) NOT NULL,
    action_name VARCHAR(16) DEFAULT 'KEEP' NOT NULL,
    retention_days INTEGER DEFAULT 90 NOT NULL,
    schedule_expression VARCHAR(100) NULL,
    maintenance_start VARCHAR(8) NULL,
    maintenance_end VARCHAR(8) NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    paused_yn CHAR(1) DEFAULT 'N' NOT NULL,
    legal_hold_yn CHAR(1) DEFAULT 'N' NOT NULL,
    chunk_size INTEGER DEFAULT 1000 NOT NULL,
    throttle_millis BIGINT DEFAULT 0 NOT NULL,
    max_rows_per_run BIGINT DEFAULT 100000 NOT NULL,
    max_runtime_seconds BIGINT DEFAULT 300 NOT NULL,
    lease_seconds INTEGER DEFAULT 60 NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    next_run_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    last_run_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    lease_owner VARCHAR(128) NULL,
    lease_until TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    fencing_token BIGINT DEFAULT 0 NOT NULL,
    row_version BIGINT DEFAULT 0 NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_RETENTION_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT ck_ops_retention_policy_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_paused CHECK (paused_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_hold CHECK (legal_hold_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_action CHECK (action_name IN ('KEEP','ARCHIVE','PURGE')),
    CONSTRAINT ck_ops_retention_policy_chunk CHECK (chunk_size >= 1 AND chunk_size <= 100000),
    CONSTRAINT ck_ops_retention_policy_limits CHECK (max_rows_per_run >= 1 AND max_runtime_seconds >= 1 AND lease_seconds >= 5)
);
COMMENT ON TABLE OPS_RETENTION_POLICY IS 'Shared retention policy, schedule and single-executor lease state';
COMMENT ON COLUMN OPS_RETENTION_POLICY.policy_id IS 'Retention policy identity';
COMMENT ON COLUMN OPS_RETENTION_POLICY.target_name IS 'Data family/handler target';
COMMENT ON COLUMN OPS_RETENTION_POLICY.action_name IS 'KEEP/ARCHIVE/PURGE';
COMMENT ON COLUMN OPS_RETENTION_POLICY.retention_days IS 'Cutoff age in days';
COMMENT ON COLUMN OPS_RETENTION_POLICY.schedule_expression IS 'Spring cron expression in UTC';
COMMENT ON COLUMN OPS_RETENTION_POLICY.maintenance_start IS 'UTC HH:mm[:ss] window start';
COMMENT ON COLUMN OPS_RETENTION_POLICY.maintenance_end IS 'UTC HH:mm[:ss] window end';
COMMENT ON COLUMN OPS_RETENTION_POLICY.enabled_yn IS 'Scheduling/execution enabled';
COMMENT ON COLUMN OPS_RETENTION_POLICY.paused_yn IS 'Policy scheduling paused';
COMMENT ON COLUMN OPS_RETENTION_POLICY.legal_hold_yn IS 'Legal hold disables destructive work';
COMMENT ON COLUMN OPS_RETENTION_POLICY.chunk_size IS 'Rows per committed chunk';
COMMENT ON COLUMN OPS_RETENTION_POLICY.throttle_millis IS 'Sleep between committed chunks';
COMMENT ON COLUMN OPS_RETENTION_POLICY.max_rows_per_run IS 'Per-run row processing limit';
COMMENT ON COLUMN OPS_RETENTION_POLICY.max_runtime_seconds IS 'Per-run wall clock limit';
COMMENT ON COLUMN OPS_RETENTION_POLICY.lease_seconds IS 'Single executor lease duration';
COMMENT ON COLUMN OPS_RETENTION_POLICY.policy_version IS 'Operator policy version';
COMMENT ON COLUMN OPS_RETENTION_POLICY.next_run_at IS 'Next scheduler due time';
COMMENT ON COLUMN OPS_RETENTION_POLICY.last_run_at IS 'Last execution completion/release time';
COMMENT ON COLUMN OPS_RETENTION_POLICY.lease_owner IS 'Current executor runtime instance';
COMMENT ON COLUMN OPS_RETENTION_POLICY.lease_until IS 'Executor lease expiry';
COMMENT ON COLUMN OPS_RETENTION_POLICY.fencing_token IS 'Monotonic executor fencing token';
COMMENT ON COLUMN OPS_RETENTION_POLICY.row_version IS 'Optimistic metadata version';
COMMENT ON COLUMN OPS_RETENTION_POLICY.created_by IS 'Creator';
COMMENT ON COLUMN OPS_RETENTION_POLICY.created_at IS 'Created time';
COMMENT ON COLUMN OPS_RETENTION_POLICY.updated_by IS 'Last updater';
COMMENT ON COLUMN OPS_RETENTION_POLICY.updated_at IS 'Last updated time';
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
    cutoff_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    started_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    completed_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    matched_count BIGINT DEFAULT 0 NOT NULL,
    archived_count BIGINT DEFAULT 0 NOT NULL,
    deleted_count BIGINT DEFAULT 0 NOT NULL,
    processed_count BIGINT DEFAULT 0 NOT NULL,
    compressed_count BIGINT DEFAULT 0 NOT NULL,
    freed_bytes BIGINT DEFAULT 0 NOT NULL,
    pause_requested_yn CHAR(1) DEFAULT 'N' NOT NULL,
    error_code VARCHAR(100) NULL,
    error_summary VARCHAR(500) NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_RETENTION_RUN PRIMARY KEY (run_id),
    CONSTRAINT ck_ops_retention_run_trigger CHECK (trigger_type IN ('SCHEDULED','MANUAL','RESUME')),
    CONSTRAINT ck_ops_retention_run_status CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','PAUSED','SKIPPED','FAILED')),
    CONSTRAINT ck_ops_retention_run_pause CHECK (pause_requested_yn IN ('Y','N')),
    CONSTRAINT fk_ops_retention_run_policy FOREIGN KEY (policy_id) REFERENCES OPS_RETENTION_POLICY (policy_id)
);
COMMENT ON TABLE OPS_RETENTION_RUN IS 'Retention run history and actual execution result';
COMMENT ON COLUMN OPS_RETENTION_RUN.run_id IS 'Retention execution identity';
COMMENT ON COLUMN OPS_RETENTION_RUN.policy_id IS 'Retention policy';
COMMENT ON COLUMN OPS_RETENTION_RUN.trigger_type IS 'SCHEDULED/MANUAL/RESUME';
COMMENT ON COLUMN OPS_RETENTION_RUN.status IS 'Run lifecycle';
COMMENT ON COLUMN OPS_RETENTION_RUN.runtime_instance_id IS 'Central runtime instance executing the run';
COMMENT ON COLUMN OPS_RETENTION_RUN.actor_id IS 'Operator/scheduler actor';
COMMENT ON COLUMN OPS_RETENTION_RUN.reason IS 'Execution reason';
COMMENT ON COLUMN OPS_RETENTION_RUN.policy_version IS 'Policy version captured at execution';
COMMENT ON COLUMN OPS_RETENTION_RUN.cutoff_at IS 'Retention cutoff';
COMMENT ON COLUMN OPS_RETENTION_RUN.started_at IS 'Run start';
COMMENT ON COLUMN OPS_RETENTION_RUN.completed_at IS 'Run completion';
COMMENT ON COLUMN OPS_RETENTION_RUN.matched_count IS 'Eligible rows observed';
COMMENT ON COLUMN OPS_RETENTION_RUN.archived_count IS 'Archived rows';
COMMENT ON COLUMN OPS_RETENTION_RUN.deleted_count IS 'Deleted rows';
COMMENT ON COLUMN OPS_RETENTION_RUN.processed_count IS 'Committed processed rows';
COMMENT ON COLUMN OPS_RETENTION_RUN.compressed_count IS 'Compressed artifacts if applicable';
COMMENT ON COLUMN OPS_RETENTION_RUN.freed_bytes IS 'Freed bytes if measurable';
COMMENT ON COLUMN OPS_RETENTION_RUN.pause_requested_yn IS 'Pause requested; honored at chunk boundary';
COMMENT ON COLUMN OPS_RETENTION_RUN.error_code IS 'Sanitized failure code';
COMMENT ON COLUMN OPS_RETENTION_RUN.error_summary IS 'Sanitized failure summary';
COMMENT ON COLUMN OPS_RETENTION_RUN.created_at IS 'Created time';
COMMENT ON COLUMN OPS_RETENTION_RUN.updated_at IS 'Last updated time';
CREATE INDEX ix_ops_retention_run_policy_time ON OPS_RETENTION_RUN (policy_id, started_at);
CREATE INDEX ix_ops_retention_run_status_time ON OPS_RETENTION_RUN (status, started_at);
CREATE INDEX ix_ops_retention_run_runtime ON OPS_RETENTION_RUN (runtime_instance_id, started_at);
