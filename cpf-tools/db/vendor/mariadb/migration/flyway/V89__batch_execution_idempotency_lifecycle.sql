ALTER TABLE CPF_BATCH_EXECUTION_CONTROL
  ADD COLUMN idempotency_scope VARCHAR(400) NULL,
  ADD COLUMN request_hash CHAR(64) NULL,
  ADD COLUMN plan_checksum CHAR(64) NULL,
  ADD COLUMN control_version BIGINT NOT NULL DEFAULT 1,
  ADD COLUMN reconcile_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN reconcile_after DATETIME(6) NULL,
  ADD COLUMN last_error_code VARCHAR(100) NULL,
  ADD COLUMN last_error_detail VARCHAR(4000) NULL;

-- 기존 실행은 보존하되 canonical payload가 없으므로 zero digest로 fail-closed 재사용합니다.
UPDATE CPF_BATCH_EXECUTION_CONTROL
   SET idempotency_scope = CONCAT(job_id, ':', definition_version, ':', approval_id),
       request_hash = REPEAT('0', 64),
       plan_checksum = REPEAT('0', 64),
       last_error_code = CASE WHEN control_status IN ('COMPLETED','FAILED','STOPPED','ABANDONED','REJECTED')
                              THEN last_error_code ELSE 'LEGACY_EXECUTION_REQUIRES_RECONCILIATION' END
 WHERE idempotency_scope IS NULL;

ALTER TABLE CPF_BATCH_EXECUTION_CONTROL
  MODIFY idempotency_scope VARCHAR(400) NOT NULL,
  MODIFY request_hash CHAR(64) NOT NULL,
  MODIFY plan_checksum CHAR(64) NOT NULL;

ALTER TABLE CPF_BATCH_EXECUTION_CONTROL DROP INDEX idempotency_key;
CREATE UNIQUE INDEX UK_CPF_BAT_EXEC_IDEM_SCOPE
  ON CPF_BATCH_EXECUTION_CONTROL(idempotency_scope, idempotency_key);
CREATE INDEX IX_CPF_BAT_EXEC_RECONCILE
  ON CPF_BATCH_EXECUTION_CONTROL(control_status, reconcile_after, updated_at);
ALTER TABLE CPF_BATCH_EXECUTION_CONTROL
  ADD CONSTRAINT CK_CPF_BAT_REQUEST_HASH CHECK (request_hash REGEXP '^[0-9a-f]{64}$'),
  ADD CONSTRAINT CK_CPF_BAT_PLAN_HASH CHECK (plan_checksum REGEXP '^[0-9a-f]{64}$'),
  ADD CONSTRAINT CK_CPF_BAT_CONTROL_VERSION CHECK (control_version > 0),
  ADD CONSTRAINT CK_CPF_BAT_RECONCILE_ATTEMPT CHECK (reconcile_attempts >= 0);

CREATE TABLE CPF_BATCH_EXECUTION_EPOCH (
  job_id VARCHAR(80) NOT NULL,
  current_fencing_token BIGINT NOT NULL,
  epoch_version BIGINT NOT NULL DEFAULT 1,
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (job_id),
  CONSTRAINT CK_CPF_BAT_EPOCH_TOKEN CHECK (current_fencing_token > 0),
  CONSTRAINT CK_CPF_BAT_EPOCH_VERSION CHECK (epoch_version > 0)
) ENGINE=InnoDB;

INSERT INTO CPF_BATCH_EXECUTION_EPOCH(job_id, current_fencing_token, epoch_version, updated_at)
SELECT job_id, MAX(fencing_token), 1, CURRENT_TIMESTAMP(6)
  FROM CPF_BATCH_EXECUTION_CONTROL
 GROUP BY job_id;
