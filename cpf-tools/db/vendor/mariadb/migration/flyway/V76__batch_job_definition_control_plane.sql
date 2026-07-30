CREATE TABLE bat_job_definition_version (
  job_id VARCHAR(80) NOT NULL, definition_version BIGINT NOT NULL, job_name VARCHAR(200) NOT NULL,
  executor_type VARCHAR(40) NOT NULL, definition_state VARCHAR(20) NOT NULL, owner_domain VARCHAR(80) NOT NULL,
  description VARCHAR(1000), trigger_type VARCHAR(30) NOT NULL, trigger_expression VARCHAR(500), timezone_id VARCHAR(60) NOT NULL,
  misfire_policy VARCHAR(30) NOT NULL, agent_pool VARCHAR(100) NOT NULL, zone_id VARCHAR(80), max_concurrency INT NOT NULL,
  timeout_seconds BIGINT NOT NULL, restartable_yn CHAR(1) NOT NULL, max_attempts INT NOT NULL, initial_backoff_seconds BIGINT NOT NULL,
  backoff_multiplier DECIMAL(10,4) NOT NULL, max_backoff_seconds BIGINT NOT NULL, skip_limit INT NOT NULL,
  unknown_result_policy VARCHAR(30) NOT NULL, compensation_reference VARCHAR(200), alert_delay_seconds BIGINT NOT NULL,
  sla_seconds BIGINT NOT NULL, notify_failure_yn CHAR(1) NOT NULL, notify_missed_yn CHAR(1) NOT NULL,
  executor_reference VARCHAR(300) NOT NULL, definition_json LONGTEXT NOT NULL, checksum VARCHAR(128), effective_from TIMESTAMP(3) NULL,
  effective_until TIMESTAMP(3) NULL, row_version BIGINT NOT NULL, created_by VARCHAR(100) NOT NULL, created_at TIMESTAMP(3) NOT NULL,
  updated_by VARCHAR(100) NOT NULL, updated_at TIMESTAMP(3) NOT NULL,
  PRIMARY KEY(job_id,definition_version),
  CONSTRAINT ck_bat_job_def_state CHECK(definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED','RETIRED')),
  CONSTRAINT ck_bat_job_def_yesno CHECK(restartable_yn IN ('Y','N') AND notify_failure_yn IN ('Y','N') AND notify_missed_yn IN ('Y','N')),
  CONSTRAINT ck_bat_job_def_version CHECK(definition_version>0 AND row_version>0)
);
CREATE INDEX idx_bat_job_def_state ON bat_job_definition_version(definition_state,updated_at);
CREATE INDEX idx_bat_job_def_owner ON bat_job_definition_version(owner_domain,job_id);
CREATE TABLE bat_job_parameter_definition (
  job_id VARCHAR(80) NOT NULL, definition_version BIGINT NOT NULL, parameter_name VARCHAR(100) NOT NULL,
  parameter_type VARCHAR(40) NOT NULL, label_text VARCHAR(200), description_text VARCHAR(1000), required_yn CHAR(1) NOT NULL,
  sensitive_yn CHAR(1) NOT NULL, default_value VARCHAR(1000), allowed_values LONGTEXT, validation_pattern VARCHAR(1000),
  min_value DECIMAL(38,10), max_value DECIMAL(38,10), min_length INT, max_length INT, reference_type VARCHAR(80),
  alias_required_yn CHAR(1) NOT NULL, runtime_override_allowed_yn CHAR(1) NOT NULL, sort_order INT NOT NULL,
  PRIMARY KEY(job_id,definition_version,parameter_name),
  CONSTRAINT fk_bat_job_param_def FOREIGN KEY(job_id,definition_version) REFERENCES bat_job_definition_version(job_id,definition_version) ON DELETE CASCADE,
  CONSTRAINT ck_bat_job_param_yesno CHECK(required_yn IN ('Y','N') AND sensitive_yn IN ('Y','N') AND alias_required_yn IN ('Y','N') AND runtime_override_allowed_yn IN ('Y','N'))
);
CREATE TABLE bat_job_dependency (
  job_id VARCHAR(80) NOT NULL, definition_version BIGINT NOT NULL, related_job_id VARCHAR(80) NOT NULL,
  condition_code VARCHAR(40) NOT NULL, timeout_seconds BIGINT NOT NULL, required_yn CHAR(1) NOT NULL, sort_order INT NOT NULL,
  PRIMARY KEY(job_id,definition_version,related_job_id),
  CONSTRAINT fk_bat_job_dep_def FOREIGN KEY(job_id,definition_version) REFERENCES bat_job_definition_version(job_id,definition_version) ON DELETE CASCADE,
  CONSTRAINT ck_bat_job_dep_self CHECK(job_id<>related_job_id), CONSTRAINT ck_bat_job_dep_yesno CHECK(required_yn IN ('Y','N'))
);
CREATE TABLE bat_job_definition_audit (
  audit_id BIGINT NOT NULL AUTO_INCREMENT, job_id VARCHAR(80) NOT NULL, definition_version BIGINT NOT NULL,
  action_code VARCHAR(40) NOT NULL, from_state VARCHAR(20), to_state VARCHAR(20), reason VARCHAR(1000) NOT NULL,
  operator_id VARCHAR(100) NOT NULL, created_at TIMESTAMP(3) NOT NULL, PRIMARY KEY(audit_id)
);
CREATE INDEX idx_bat_job_def_audit ON bat_job_definition_audit(job_id,definition_version,created_at);
