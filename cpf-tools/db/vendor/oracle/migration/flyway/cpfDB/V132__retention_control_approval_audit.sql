-- CPF V132: Retention control approval/fencing/audit hardening.
ALTER TABLE OPS_RETENTION_RUN ADD (control_actor_id VARCHAR2(100 CHAR) NULL, control_reason VARCHAR2(500 CHAR) NULL);
CREATE TABLE OPS_RETENTION_CONTROL_AUDIT (
  audit_id VARCHAR2(64 CHAR) NOT NULL, operation_type VARCHAR2(40 CHAR) NOT NULL, target_type VARCHAR2(20 CHAR) NOT NULL, target_id VARCHAR2(80 CHAR) NOT NULL,
  requested_by VARCHAR2(100 CHAR) NOT NULL, approved_by VARCHAR2(100 CHAR) NULL, approval_request_id VARCHAR2(120 CHAR) NULL, reason_text VARCHAR2(500 CHAR) NOT NULL,
  expected_version NUMBER(19) NULL, result_state VARCHAR2(20 CHAR) DEFAULT 'SUCCEEDED' NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
  CONSTRAINT PK_OPS_RETENTION_CONTROL_AUDIT PRIMARY KEY (audit_id),
  CONSTRAINT ck_ops_retention_audit_result CHECK (result_state IN ('SUCCEEDED','FAILED','UNKNOWN'))
);
CREATE INDEX ix_ops_retention_audit_target ON OPS_RETENTION_CONTROL_AUDIT(target_type,target_id,created_at);
CREATE INDEX ix_ops_retention_audit_approval ON OPS_RETENTION_CONTROL_AUDIT(approval_request_id,created_at);
