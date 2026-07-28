-- RTF-038 reconciliation distributed claim columns
ALTER TABLE cpf_unknown_result
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN next_check_at DATETIME(3) NULL,
    ADD COLUMN lease_owner VARCHAR(120) NULL,
    ADD COLUMN lease_until DATETIME(3) NULL,
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX ix_cpf_unknown_result_claim ON cpf_unknown_result (unknown_status, next_check_at, lease_until, detected_at);

-- CPF Enterprise QA V64 - Runtime Control Plane / durable cache checkpoint (MariaDB)
ALTER TABLE cpf_service ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cpf_service_endpoint ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cpf_service_instance
    ADD COLUMN environment_code VARCHAR(40) NOT NULL DEFAULT 'default',
    ADD COLUMN zone_code VARCHAR(60) NULL,
    ADD COLUMN cell_code VARCHAR(60) NULL,
    ADD COLUMN priority_no INT NOT NULL DEFAULT 100,
    ADD COLUMN maintenance_yn CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN drain_yn CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN drain_deadline_at DATETIME(3) NULL,
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX ix_cpf_service_instance_placement ON cpf_service_instance(environment_code,zone_code,cell_code,active_yn,instance_status);
CREATE INDEX ix_cpf_service_instance_route ON cpf_service_instance(endpoint_code,priority_no,maintenance_yn,drain_yn,active_yn,instance_status);
ALTER TABLE cpf_service_instance ADD CONSTRAINT ck_cpf_service_instance_maintenance CHECK(maintenance_yn IN ('Y','N'));
ALTER TABLE cpf_service_instance ADD CONSTRAINT ck_cpf_service_instance_drain CHECK(drain_yn IN ('Y','N'));

CREATE TABLE cpf_runtime_version (
  version_key VARCHAR(40) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(version_key)
);
INSERT INTO cpf_runtime_version(version_key,version_no) VALUES ('GLOBAL',0);


CREATE TABLE cpf_runtime_controller_lease (
  lease_key VARCHAR(60) NOT NULL, holder_id VARCHAR(120) NOT NULL,
  fencing_token BIGINT NOT NULL DEFAULT 0, lease_until DATETIME(3) NOT NULL, last_reconciled_at DATETIME(3) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(lease_key)
);
CREATE INDEX ix_cpf_runtime_controller_lease_until ON cpf_runtime_controller_lease(lease_until);


CREATE TABLE cpf_runtime_rate_bucket (
  bucket_key VARCHAR(180) NOT NULL, subject_id VARCHAR(120) NOT NULL,
  window_start DATETIME(3) NOT NULL, request_count INT NOT NULL DEFAULT 0,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(bucket_key), CONSTRAINT ck_cpf_runtime_rate_count CHECK(request_count >= 0)
);
CREATE INDEX ix_cpf_runtime_rate_subject ON cpf_runtime_rate_bucket(subject_id,window_start);

CREATE TABLE cpf_runtime_instance_group (
  group_id VARCHAR(80) NOT NULL, group_name VARCHAR(150) NOT NULL, parent_group_id VARCHAR(80) NULL,
  environment_code VARCHAR(40) NULL, description VARCHAR(500) NULL, active_yn CHAR(1) NOT NULL DEFAULT 'Y', row_version BIGINT NOT NULL DEFAULT 0,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(group_id), CONSTRAINT fk_cpf_runtime_group_parent FOREIGN KEY(parent_group_id) REFERENCES cpf_runtime_instance_group(group_id),
  CONSTRAINT ck_cpf_runtime_group_active CHECK(active_yn IN ('Y','N'))
);
CREATE INDEX ix_cpf_runtime_group_parent ON cpf_runtime_instance_group(parent_group_id,active_yn);
CREATE INDEX ix_cpf_runtime_group_env ON cpf_runtime_instance_group(environment_code,active_yn);

CREATE TABLE cpf_runtime_group_member (
  group_id VARCHAR(80) NOT NULL, instance_id VARCHAR(120) NOT NULL, active_yn CHAR(1) NOT NULL DEFAULT 'Y',
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(group_id,instance_id),
  CONSTRAINT fk_cpf_runtime_group_member_group FOREIGN KEY(group_id) REFERENCES cpf_runtime_instance_group(group_id) ON DELETE CASCADE,
  CONSTRAINT fk_cpf_runtime_group_member_instance FOREIGN KEY(instance_id) REFERENCES cpf_service_instance(instance_id) ON DELETE CASCADE,
  CONSTRAINT ck_cpf_runtime_group_member_active CHECK(active_yn IN ('Y','N'))
);
CREATE INDEX ix_cpf_runtime_group_member_instance ON cpf_runtime_group_member(instance_id,active_yn);

CREATE TABLE cpf_runtime_instance_state (
  instance_id VARCHAR(120) NOT NULL, fencing_token BIGINT NOT NULL DEFAULT 0, lease_until DATETIME(3) NULL,
  desired_version BIGINT NOT NULL DEFAULT 0, actual_version BIGINT NOT NULL DEFAULT 0, desired_hash VARCHAR(64) NULL, actual_hash VARCHAR(64) NULL,
  drift_state VARCHAR(30) NOT NULL DEFAULT 'IN_SYNC', capabilities_json LONGTEXT NULL, labels_json LONGTEXT NULL,
  artifact_version VARCHAR(100) NULL, artifact_commit VARCHAR(64) NULL, runtime_role VARCHAR(40) NULL,
  registration_source VARCHAR(120) NULL, schema_version VARCHAR(100) NULL, config_hash VARCHAR(64) NULL,
  clock_skew_ms BIGINT NOT NULL DEFAULT 0,
  last_ack_change_id VARCHAR(80) NULL, last_ack_at DATETIME(3) NULL, heartbeat_at DATETIME(3) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(instance_id), CONSTRAINT fk_cpf_runtime_instance_state_instance FOREIGN KEY(instance_id) REFERENCES cpf_service_instance(instance_id) ON DELETE CASCADE,
  CONSTRAINT ck_cpf_runtime_instance_drift CHECK(drift_state IN ('IN_SYNC','PENDING','DRIFT','UNKNOWN','UNKNOWN_RESULT','PENDING_RESTART','EXCLUDED'))
);
CREATE INDEX ix_cpf_runtime_instance_lease ON cpf_runtime_instance_state(lease_until);
CREATE INDEX ix_cpf_runtime_instance_drift ON cpf_runtime_instance_state(drift_state,heartbeat_at);


CREATE TABLE cpf_runtime_instance_feature_state (
  instance_id VARCHAR(120) NOT NULL, change_type VARCHAR(80) NOT NULL,
  desired_version BIGINT NOT NULL DEFAULT 0, actual_version BIGINT NOT NULL DEFAULT 0,
  desired_hash VARCHAR(64) NULL, actual_hash VARCHAR(64) NULL,
  drift_state VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN', source_delivery_id VARCHAR(80) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(instance_id,change_type),
  CONSTRAINT fk_cpf_runtime_feature_instance FOREIGN KEY(instance_id) REFERENCES cpf_service_instance(instance_id) ON DELETE CASCADE,
  CONSTRAINT ck_cpf_runtime_feature_drift CHECK(drift_state IN ('IN_SYNC','PENDING','DRIFT','UNKNOWN','UNKNOWN_RESULT','PENDING_RESTART','EXCLUDED'))
);
CREATE INDEX ix_cpf_runtime_feature_drift ON cpf_runtime_instance_feature_state(drift_state,change_type);
CREATE INDEX ix_cpf_runtime_feature_delivery ON cpf_runtime_instance_feature_state(source_delivery_id);

CREATE TABLE cpf_control_operation (
  operation_id VARCHAR(100) NOT NULL, command_type VARCHAR(80) NOT NULL, request_hash VARCHAR(64) NOT NULL,
  entity_id VARCHAR(100) NULL, result_state VARCHAR(30) NOT NULL DEFAULT 'PROCESSING', result_json LONGTEXT NULL, expires_at DATETIME(3) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(operation_id), CONSTRAINT ck_cpf_control_operation_state CHECK(result_state IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED','CANCELLED'))
);
CREATE INDEX ix_cpf_control_operation_expiry ON cpf_control_operation(result_state,expires_at);

CREATE TABLE cpf_runtime_change (
  change_id VARCHAR(80) NOT NULL, operation_id VARCHAR(100) NOT NULL, change_type VARCHAR(80) NOT NULL, payload_schema_version INT NOT NULL DEFAULT 1,
  request_hash VARCHAR(64) NOT NULL, payload_hash VARCHAR(64) NOT NULL, payload_json LONGTEXT NOT NULL, rollback_payload_json LONGTEXT NULL, target_snapshot_json LONGTEXT NOT NULL,
  desired_version BIGINT NOT NULL, rollout_mode VARCHAR(30) NOT NULL DEFAULT 'ALL_AT_ONCE', wave_size INT NOT NULL DEFAULT 100, quorum_percent INT NOT NULL DEFAULT 100,
  change_state VARCHAR(30) NOT NULL DEFAULT 'APPLYING', scheduled_at DATETIME(3) NULL, expires_at DATETIME(3) NULL,
  reason VARCHAR(1000) NOT NULL, approval_id VARCHAR(100) NULL, break_glass_id VARCHAR(100) NULL, requested_by VARCHAR(100) NOT NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(change_id), UNIQUE KEY ux_cpf_runtime_change_operation(operation_id),
  CONSTRAINT fk_cpf_runtime_change_operation FOREIGN KEY(operation_id) REFERENCES cpf_control_operation(operation_id),
  CONSTRAINT ck_cpf_runtime_change_state CHECK(change_state IN ('SCHEDULED','APPLYING','PARTIAL','SUCCESS','FAILED','CANCELLED','EXPIRED','ROLLBACK_PENDING','ROLLED_BACK','SUPERSEDED','UNKNOWN_RESULT','RECOVERED'))
);
CREATE INDEX ix_cpf_runtime_change_state ON cpf_runtime_change(change_state,scheduled_at,expires_at);

CREATE TABLE cpf_runtime_delivery (
  delivery_id VARCHAR(80) NOT NULL, change_id VARCHAR(80) NOT NULL, instance_id VARCHAR(120) NOT NULL, sequence_no INT NOT NULL,
  desired_version BIGINT NOT NULL, delivery_state VARCHAR(30) NOT NULL DEFAULT 'PENDING', attempt_no INT NOT NULL DEFAULT 0,
  next_attempt_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), fencing_token BIGINT NULL, claimed_at DATETIME(3) NULL, acknowledged_at DATETIME(3) NULL,
  actual_hash VARCHAR(64) NULL, error_code VARCHAR(80) NULL, error_message VARCHAR(900) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(delivery_id),
  CONSTRAINT fk_cpf_runtime_delivery_change FOREIGN KEY(change_id) REFERENCES cpf_runtime_change(change_id) ON DELETE CASCADE,
  CONSTRAINT fk_cpf_runtime_delivery_instance FOREIGN KEY(instance_id) REFERENCES cpf_service_instance(instance_id) ON DELETE CASCADE,
  CONSTRAINT ck_cpf_runtime_delivery_state CHECK(delivery_state IN ('PENDING','CLAIMED','ACKED','FAILED','POISONED','UNKNOWN_RESULT','RESTART_REQUIRED','CANCELLED','EXPIRED','SUPERSEDED'))
);
CREATE INDEX ix_cpf_runtime_delivery_claim ON cpf_runtime_delivery(instance_id,delivery_state,next_attempt_at,sequence_no);
CREATE INDEX ix_cpf_runtime_delivery_change ON cpf_runtime_delivery(change_id,delivery_state);

CREATE TABLE cpf_runtime_change_audit (
  audit_id BIGINT NOT NULL AUTO_INCREMENT, change_id VARCHAR(80) NOT NULL, event_type VARCHAR(60) NOT NULL, actor_id VARCHAR(100) NOT NULL,
  reason VARCHAR(500) NULL, evidence_hash VARCHAR(64) NULL, previous_hash VARCHAR(64) NOT NULL, chain_hash VARCHAR(64) NOT NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(audit_id), CONSTRAINT fk_cpf_runtime_audit_change FOREIGN KEY(change_id) REFERENCES cpf_runtime_change(change_id) ON DELETE CASCADE
);
CREATE INDEX ix_cpf_runtime_change_audit_change ON cpf_runtime_change_audit(change_id,audit_id);

CREATE TABLE cpf_cache_refresh_checkpoint (
  consumer_id VARCHAR(120) NOT NULL, last_event_id BIGINT NOT NULL DEFAULT 0, last_applied_at DATETIME(3) NULL,
  created_by VARCHAR(100) NOT NULL DEFAULT 'CPF', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF', updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(consumer_id)
);
CREATE INDEX ix_cpf_cache_refresh_checkpoint_event ON cpf_cache_refresh_checkpoint(last_event_id);
