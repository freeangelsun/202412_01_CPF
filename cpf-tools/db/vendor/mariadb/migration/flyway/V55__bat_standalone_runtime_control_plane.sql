USE batDB;


CREATE TABLE IF NOT EXISTS bat_runtime_instance (instance_id VARCHAR(160) PRIMARY KEY,runtime_role VARCHAR(40) NOT NULL,service_id VARCHAR(120) NOT NULL,was_id VARCHAR(120),host_alias VARCHAR(160),zone_id VARCHAR(80),pool_id VARCHAR(80),artifact_version VARCHAR(80) NOT NULL,git_sha VARCHAR(64),artifact_checksum VARCHAR(128),profile_name VARCHAR(80),desired_state VARCHAR(32) NOT NULL DEFAULT 'RUNNING',actual_state VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',config_version VARCHAR(80),schema_compatibility VARCHAR(120),started_at DATETIME(6),last_heartbeat_at DATETIME(6),fencing_token BIGINT NOT NULL DEFAULT 0,row_version BIGINT NOT NULL DEFAULT 0,created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),KEY ix_bat_runtime_instance_service(service_id,actual_state),KEY ix_bat_runtime_instance_heartbeat(last_heartbeat_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_runtime_capability (instance_id VARCHAR(160) NOT NULL,capability_code VARCHAR(80) NOT NULL,PRIMARY KEY(instance_id,capability_code),CONSTRAINT fk_bat_runtime_capability_instance FOREIGN KEY(instance_id) REFERENCES bat_runtime_instance(instance_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_runtime_heartbeat (heartbeat_id BIGINT AUTO_INCREMENT PRIMARY KEY,instance_id VARCHAR(160) NOT NULL,heartbeat_at DATETIME(6) NOT NULL,ready_yn CHAR(1) NOT NULL,available_capacity INT NOT NULL DEFAULT 0,queue_depth BIGINT NOT NULL DEFAULT 0,draining_yn CHAR(1) NOT NULL DEFAULT 'N',current_execution_count INT NOT NULL DEFAULT 0,active_lease_count INT NOT NULL DEFAULT 0,last_error_code VARCHAR(80),deployment_version VARCHAR(80),KEY ix_bat_runtime_heartbeat_instance(instance_id,heartbeat_at),CONSTRAINT fk_bat_runtime_heartbeat_instance FOREIGN KEY(instance_id) REFERENCES bat_runtime_instance(instance_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_runtime_command (command_id VARCHAR(80) PRIMARY KEY,idempotency_key VARCHAR(160) NOT NULL UNIQUE,command_type VARCHAR(80) NOT NULL,target_type VARCHAR(40) NOT NULL,target_snapshot_hash VARCHAR(128),expected_version BIGINT,requested_by VARCHAR(120) NOT NULL,reason_text VARCHAR(1000) NOT NULL,approval_request_id VARCHAR(80),approved_by VARCHAR(120),command_state VARCHAR(40) NOT NULL,execution_attempt INT NOT NULL DEFAULT 0,failure_stage VARCHAR(80),result_code VARCHAR(80),requested_at DATETIME(6) NOT NULL,expires_at DATETIME(6),updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),transaction_id CHAR(34),evidence_ref VARCHAR(500)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_scheduler_lease (scheduler_key VARCHAR(100) PRIMARY KEY,owner_instance_id VARCHAR(160) NOT NULL,fencing_token BIGINT NOT NULL,lease_until DATETIME(6) NOT NULL,last_heartbeat_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),KEY ix_bat_scheduler_lease_expire(lease_until)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_schedule_trigger (schedule_id VARCHAR(100) NOT NULL,scheduled_fire_at DATETIME(6) NOT NULL,fencing_token BIGINT NOT NULL,execution_id BIGINT,trigger_status VARCHAR(30) NOT NULL,created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),PRIMARY KEY(schedule_id,scheduled_fire_at),CONSTRAINT fk_bat_schedule_trigger_schedule FOREIGN KEY(schedule_id) REFERENCES bat_schedule(schedule_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_center_cut_claim (center_cut_item_id BIGINT PRIMARY KEY,runner_id VARCHAR(160) NOT NULL,pool_id VARCHAR(80),claim_token VARCHAR(80) NOT NULL UNIQUE,claim_status VARCHAR(30) NOT NULL,fencing_token BIGINT NOT NULL,lease_until DATETIME(6) NOT NULL,last_heartbeat_at DATETIME(6) NOT NULL,attempt_no INT NOT NULL DEFAULT 1,takeover_count INT NOT NULL DEFAULT 0,released_at DATETIME(6),updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),CONSTRAINT fk_bat_center_cut_claim_item FOREIGN KEY(center_cut_item_id) REFERENCES bat_center_cut_item(center_cut_item_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_deployment_cell (cell_id VARCHAR(120) PRIMARY KEY,environment_id VARCHAR(80) NOT NULL,runtime_role VARCHAR(40) NOT NULL,service_id VARCHAR(120) NOT NULL,manifest_version VARCHAR(80) NOT NULL,manifest_hash VARCHAR(128) NOT NULL,desired_state VARCHAR(32) NOT NULL,row_version BIGINT NOT NULL DEFAULT 0,created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_deployment_instance (cell_id VARCHAR(120) NOT NULL,instance_id VARCHAR(160) NOT NULL,host_alias VARCHAR(160) NOT NULL,port_no INT NOT NULL,profile_name VARCHAR(80) NOT NULL,zone_id VARCHAR(80),pool_id VARCHAR(80),agent_base_url VARCHAR(500) NOT NULL,config_ref VARCHAR(1000),desired_state VARCHAR(32) NOT NULL,PRIMARY KEY(cell_id,instance_id),UNIQUE KEY uk_bat_deployment_instance_id(instance_id),CONSTRAINT fk_bat_deployment_instance_cell FOREIGN KEY(cell_id) REFERENCES bat_deployment_cell(cell_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_deployment_plan (plan_id VARCHAR(80) PRIMARY KEY,cell_id VARCHAR(120) NOT NULL,manifest_json LONGTEXT NOT NULL,manifest_hash VARCHAR(128) NOT NULL,requested_by VARCHAR(120) NOT NULL,reason_text VARCHAR(1000) NOT NULL,plan_state VARCHAR(40) NOT NULL,created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_deployment_lock (cell_id VARCHAR(120) PRIMARY KEY,owner_deployment_id VARCHAR(80) NOT NULL,fencing_token BIGINT NOT NULL,locked_at DATETIME(6) NOT NULL,expires_at DATETIME(6) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS bat_version_compatibility (compatibility_id BIGINT AUTO_INCREMENT PRIMARY KEY,environment_id VARCHAR(80) NOT NULL DEFAULT '*',provider_coordinate VARCHAR(200) NOT NULL,consumer_coordinate VARCHAR(200) NOT NULL DEFAULT '*',min_version VARCHAR(80),max_version VARCHAR(80),schema_range VARCHAR(120),required_capability VARCHAR(80),enabled_yn CHAR(1) NOT NULL DEFAULT 'Y') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE bat_execution_lease ADD COLUMN IF NOT EXISTS fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'monotonic fencing token' AFTER takeover_count;
ALTER TABLE bat_runtime_command ADD COLUMN IF NOT EXISTS target_snapshot LONGTEXT NULL AFTER target_type;
ALTER TABLE bat_runtime_command ADD COLUMN IF NOT EXISTS approval_policy_version VARCHAR(80) NULL AFTER reason_text;
ALTER TABLE bat_runtime_command ADD COLUMN IF NOT EXISTS result_text LONGTEXT NULL AFTER execution_attempt;
ALTER TABLE bat_runtime_command ADD COLUMN IF NOT EXISTS before_state LONGTEXT NULL AFTER failure_stage;
ALTER TABLE bat_runtime_command ADD COLUMN IF NOT EXISTS after_state LONGTEXT NULL AFTER before_state;
ALTER TABLE bat_execution ADD COLUMN IF NOT EXISTS stop_requested_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '운영 중지 요청 여부' AFTER retry_count;

CREATE TABLE IF NOT EXISTS bat_runtime_command_attempt (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT,
    command_id VARCHAR(80) NOT NULL,
    attempt_no INT NOT NULL,
    instance_id VARCHAR(160) NULL,
    stage_code VARCHAR(80) NOT NULL,
    attempt_state VARCHAR(40) NOT NULL,
    result_message VARCHAR(4000) NULL,
    started_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6) NULL,
    PRIMARY KEY(attempt_id),
    UNIQUE KEY uk_bat_runtime_command_attempt(command_id,attempt_no,instance_id,stage_code),
    KEY ix_bat_runtime_command_attempt_instance(instance_id,started_at),
    CONSTRAINT fk_bat_runtime_command_attempt_command FOREIGN KEY(command_id)
      REFERENCES bat_runtime_command(command_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bat_deployment_execution (
    deployment_id VARCHAR(80) NOT NULL,
    cell_id VARCHAR(120) NOT NULL,
    idempotency_key VARCHAR(160) NOT NULL,
    from_version VARCHAR(80) NULL,
    to_version VARCHAR(80) NOT NULL,
    strategy_code VARCHAR(32) NOT NULL,
    execution_state VARCHAR(40) NOT NULL,
    failure_stage VARCHAR(80) NULL,
    result_message VARCHAR(4000) NULL,
    requested_by VARCHAR(120) NOT NULL,
    approved_by VARCHAR(120) NOT NULL,
    reason_text VARCHAR(1000) NOT NULL,
    started_at DATETIME(6) NULL,
    finished_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY(deployment_id),
    UNIQUE KEY uk_bat_deployment_execution_idempotency(idempotency_key),
    KEY ix_bat_deployment_execution_cell_state(cell_id,execution_state),
    CONSTRAINT fk_bat_deployment_execution_cell FOREIGN KEY(cell_id)
      REFERENCES bat_deployment_cell(cell_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bat_deployment_instance_result (
    deployment_result_id BIGINT NOT NULL AUTO_INCREMENT,
    deployment_id VARCHAR(80) NOT NULL,
    sequence_no INT NOT NULL,
    instance_id VARCHAR(160) NOT NULL,
    stage_code VARCHAR(80) NOT NULL,
    result_state VARCHAR(40) NOT NULL,
    result_message VARCHAR(4000) NULL,
    recorded_at DATETIME(6) NOT NULL,
    PRIMARY KEY(deployment_result_id),
    UNIQUE KEY uk_bat_deployment_instance_result(deployment_id,sequence_no),
    KEY ix_bat_deployment_instance_result_instance(instance_id,recorded_at),
    CONSTRAINT fk_bat_deployment_instance_result_execution FOREIGN KEY(deployment_id)
      REFERENCES bat_deployment_execution(deployment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
ALTER TABLE bat_deployment_cell ADD COLUMN IF NOT EXISTS desired_instance_count INT NOT NULL DEFAULT 1 AFTER desired_state;


CREATE TABLE IF NOT EXISTS bat_job_pack (
  job_pack_id VARCHAR(120) NOT NULL,owner_domain VARCHAR(20) NOT NULL,artifact_coordinate VARCHAR(240) NOT NULL,
  artifact_version VARCHAR(80) NOT NULL,artifact_checksum VARCHAR(128) NULL,signature_present_yn CHAR(1) NOT NULL DEFAULT 'N',
  platform_range VARCHAR(120) NULL,manifest_json LONGTEXT NOT NULL,last_registered_at DATETIME(6) NOT NULL,
  PRIMARY KEY(job_pack_id),KEY ix_bat_job_pack_owner(owner_domain,artifact_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS bat_job_pack_job (
  job_pack_id VARCHAR(120) NOT NULL,job_id VARCHAR(100) NOT NULL,restartable_yn CHAR(1) NOT NULL,
  center_cut_provider_key VARCHAR(100) NULL,center_cut_handler_key VARCHAR(100) NULL,
  PRIMARY KEY(job_pack_id,job_id),CONSTRAINT fk_bat_job_pack_job_pack FOREIGN KEY(job_pack_id) REFERENCES bat_job_pack(job_pack_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- R15/R16/R17 Center-Cut immutable execution/runtime policy.
CREATE TABLE IF NOT EXISTS bat_center_cut_execution (
  center_cut_execution_id VARCHAR(80) NOT NULL,
  center_cut_job_id VARCHAR(100) NOT NULL,
  idempotency_key VARCHAR(160) NOT NULL,
  execution_state VARCHAR(30) NOT NULL,
  parameter_ciphertext LONGTEXT NOT NULL,
  parameter_hash VARCHAR(64) NOT NULL,
  parameter_schema_version VARCHAR(80) NOT NULL,
  target_cursor VARCHAR(1000) NULL,
  target_complete_yn CHAR(1) NOT NULL DEFAULT 'N',
  target_count BIGINT NOT NULL DEFAULT 0,
  tps_limit INT NOT NULL DEFAULT 0,
  concurrency_limit INT NOT NULL DEFAULT 1,
  processed_count BIGINT NOT NULL DEFAULT 0,
  success_count BIGINT NOT NULL DEFAULT 0,
  failure_count BIGINT NOT NULL DEFAULT 0,
  unknown_count BIGINT NOT NULL DEFAULT 0,
  transaction_id CHAR(34) NULL,
  parent_segment_id VARCHAR(120) NULL,
  requested_by VARCHAR(120) NOT NULL,
  reason_text VARCHAR(1000) NOT NULL,
  last_error_message VARCHAR(1000) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  completed_at DATETIME(6) NULL,
  PRIMARY KEY(center_cut_execution_id),
  UNIQUE KEY uk_bat_center_cut_execution_idempotency(idempotency_key),
  KEY ix_bat_center_cut_execution_job_state(center_cut_job_id,execution_state,created_at),
  CONSTRAINT fk_bat_center_cut_execution_job FOREIGN KEY(center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bat_center_cut_rate_window (
  center_cut_execution_id VARCHAR(80) NOT NULL,
  window_second BIGINT NOT NULL,
  admitted_count INT NOT NULL DEFAULT 0,
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY(center_cut_execution_id,window_second),
  CONSTRAINT fk_bat_center_cut_rate_execution FOREIGN KEY(center_cut_execution_id)
    REFERENCES bat_center_cut_execution(center_cut_execution_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE bat_center_cut_item ADD COLUMN IF NOT EXISTS center_cut_execution_id VARCHAR(80) NULL AFTER center_cut_job_id;
ALTER TABLE bat_center_cut_item ADD INDEX IF NOT EXISTS ix_bat_center_cut_item_execution_status(center_cut_execution_id,item_status,center_cut_item_id);
ALTER TABLE bat_center_cut_item ADD CONSTRAINT fk_bat_center_cut_item_execution FOREIGN KEY(center_cut_execution_id)
  REFERENCES bat_center_cut_execution(center_cut_execution_id) ON DELETE CASCADE;


ALTER TABLE bat_center_cut_item DROP INDEX IF EXISTS uk_bat_center_cut_item_business;
ALTER TABLE bat_center_cut_item ADD UNIQUE INDEX IF NOT EXISTS uk_bat_center_cut_item_execution_business(center_cut_execution_id,business_key);
