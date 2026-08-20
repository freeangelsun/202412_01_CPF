-- CPF V132: Retention control approval/fencing/audit hardening.
ALTER TABLE OPS_RETENTION_RUN
  ADD COLUMN control_actor_id VARCHAR(100) NULL AFTER pause_requested_yn,
  ADD COLUMN control_reason VARCHAR(500) NULL AFTER control_actor_id;
CREATE TABLE OPS_RETENTION_CONTROL_AUDIT (
  audit_id VARCHAR(64) NOT NULL, operation_type VARCHAR(40) NOT NULL, target_type VARCHAR(20) NOT NULL, target_id VARCHAR(80) NOT NULL,
  requested_by VARCHAR(100) NOT NULL, approved_by VARCHAR(100) NULL, approval_request_id VARCHAR(120) NULL, reason_text VARCHAR(500) NOT NULL,
  expected_version BIGINT NULL, result_state VARCHAR(20) DEFAULT 'SUCCEEDED' NOT NULL, created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT PK_OPS_RETENTION_CONTROL_AUDIT PRIMARY KEY (audit_id),
  CONSTRAINT ck_ops_retention_audit_result CHECK (result_state IN ('SUCCEEDED','FAILED','UNKNOWN'))
) ENGINE=InnoDB;
CREATE INDEX ix_ops_retention_audit_target ON OPS_RETENTION_CONTROL_AUDIT(target_type,target_id,created_at);
CREATE INDEX ix_ops_retention_audit_approval ON OPS_RETENTION_CONTROL_AUDIT(approval_request_id,created_at);
