-- CPF QA39 resilience and feature flag control plane. Generated from cpf-tools/db/canonical/platform-schema.json.
CREATE TABLE cpf_resilience_policy (
  policy_id VARCHAR(64) NOT NULL,
  operation_id VARCHAR(200) NOT NULL,
  revision BIGINT NOT NULL,
  timeout_ms BIGINT NOT NULL,
  max_attempts BIGINT NOT NULL,
  retry_backoff_ms BIGINT NOT NULL,
  circuit_failure_threshold BIGINT NOT NULL,
  circuit_open_ms BIGINT NOT NULL,
  bulkhead_max_concurrent BIGINT NOT NULL,
  rate_limit_permits BIGINT NOT NULL,
  rate_limit_window_ms BIGINT NOT NULL,
  idempotent_flag CHAR(1) DEFAULT 'N' NOT NULL,
  reconcile_flag CHAR(1) DEFAULT 'N' NOT NULL,
  policy_status VARCHAR(20) NOT NULL,
  active_operation_key VARCHAR(200),
  updated_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_resilience_policy PRIMARY KEY (policy_id),
  CONSTRAINT uk_cpf_rpol_active UNIQUE (active_operation_key),
  CONSTRAINT uk_cpf_rpol_rev UNIQUE (operation_id, revision),
  CONSTRAINT ck_cpf_rpol_idem CHECK (idempotent_flag IN ('Y','N')),
  CONSTRAINT ck_cpf_rpol_recon CHECK (reconcile_flag IN ('Y','N'))
);
CREATE INDEX ix_cpf_rpol_status ON cpf_resilience_policy (policy_status, operation_id);

CREATE TABLE cpf_resilience_policy_request (
  request_id VARCHAR(64) NOT NULL,
  operation_id VARCHAR(200) NOT NULL,
  requested_revision BIGINT NOT NULL,
  policy_payload VARCHAR(2000) NOT NULL,
  requester_id VARCHAR(100) NOT NULL,
  request_reason VARCHAR(500) NOT NULL,
  request_status VARCHAR(20) NOT NULL,
  approver_id VARCHAR(100),
  approval_reason VARCHAR(500),
  active_operation_key VARCHAR(200),
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_resilience_policy_request PRIMARY KEY (request_id),
  CONSTRAINT uk_cpf_rpreq_pending UNIQUE (active_operation_key),
  CONSTRAINT ck_cpf_rpreq_status CHECK (request_status IN ('PENDING','APPROVED','REJECTED'))
);
CREATE INDEX ix_cpf_rpreq_status ON cpf_resilience_policy_request (request_status, created_at);

CREATE TABLE cpf_resilience_audit (
  audit_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  operation_id VARCHAR(200) NOT NULL,
  actor_id VARCHAR(100),
  reason_code VARCHAR(256),
  sanitized_attributes VARCHAR(2000),
  occurred_at TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_resilience_audit PRIMARY KEY (audit_id)
);
CREATE INDEX ix_cpf_raud_op_time ON cpf_resilience_audit (operation_id, occurred_at);

CREATE TABLE cpf_feature_flag_override_request (
  request_id VARCHAR(64) NOT NULL,
  flag_key VARCHAR(200) NOT NULL,
  value_type VARCHAR(20) NOT NULL,
  value_text VARCHAR(2000) NOT NULL,
  expires_at TIMESTAMP(3) NOT NULL,
  requester_id VARCHAR(100) NOT NULL,
  request_reason VARCHAR(500) NOT NULL,
  request_status VARCHAR(20) NOT NULL,
  approver_id VARCHAR(100),
  approval_reason VARCHAR(500),
  active_flag_key VARCHAR(200),
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_feature_flag_override_request PRIMARY KEY (request_id),
  CONSTRAINT uk_cpf_ffreq_pending UNIQUE (active_flag_key),
  CONSTRAINT ck_cpf_ffreq_status CHECK (request_status IN ('PENDING','APPROVED','REJECTED'))
);
CREATE INDEX ix_cpf_ffreq_status ON cpf_feature_flag_override_request (request_status, created_at);

CREATE TABLE cpf_feature_flag_override (
  override_id VARCHAR(64) NOT NULL,
  flag_key VARCHAR(200) NOT NULL,
  value_type VARCHAR(20) NOT NULL,
  value_text VARCHAR(2000) NOT NULL,
  expires_at TIMESTAMP(3) NOT NULL,
  override_status VARCHAR(20) NOT NULL,
  revision BIGINT NOT NULL,
  active_flag_key VARCHAR(200),
  created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_feature_flag_override PRIMARY KEY (override_id),
  CONSTRAINT uk_cpf_ffovr_active UNIQUE (active_flag_key),
  CONSTRAINT ck_cpf_ffovr_status CHECK (override_status IN ('ACTIVE','SUPERSEDED','REVOKED','EXPIRED'))
);
CREATE INDEX ix_cpf_ffovr_key ON cpf_feature_flag_override (flag_key, override_status, expires_at);

CREATE TABLE cpf_feature_flag_kill_switch (
  flag_key VARCHAR(200) NOT NULL,
  enabled_flag CHAR(1) DEFAULT 'N' NOT NULL,
  revision BIGINT NOT NULL,
  updated_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_feature_flag_kill_switch PRIMARY KEY (flag_key),
  CONSTRAINT ck_cpf_ffkill_enabled CHECK (enabled_flag IN ('Y','N'))
);

CREATE TABLE cpf_feature_flag_revision (
  singleton_id BIGINT NOT NULL,
  revision BIGINT NOT NULL,
  updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_feature_flag_revision PRIMARY KEY (singleton_id),
  CONSTRAINT ck_cpf_ffrev_single CHECK (singleton_id = 1),
  CONSTRAINT ck_cpf_ffrev_value CHECK (revision >= 0)
);

CREATE TABLE cpf_feature_flag_audit (
  audit_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  flag_key VARCHAR(200) NOT NULL,
  actor_id VARCHAR(100),
  reason_code VARCHAR(500),
  sanitized_attributes VARCHAR(2000),
  occurred_at TIMESTAMP(3) NOT NULL,
  CONSTRAINT pk_cpf_feature_flag_audit PRIMARY KEY (audit_id)
);
CREATE INDEX ix_cpf_ffaudit_key_time ON cpf_feature_flag_audit (flag_key, occurred_at);

INSERT INTO cpf_feature_flag_revision(singleton_id,revision,updated_at) VALUES (1,0,CURRENT_TIMESTAMP);
