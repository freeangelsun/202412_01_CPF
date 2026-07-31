ALTER TABLE CPF_BATCH_EXECUTION_CONTROL ADD (
  idempotency_scope VARCHAR2(400 CHAR),
  request_hash CHAR(64 CHAR),
  plan_checksum CHAR(64 CHAR),
  control_version NUMBER(19) DEFAULT 1 NOT NULL,
  reconcile_attempts NUMBER(10) DEFAULT 0 NOT NULL,
  reconcile_after TIMESTAMP WITH TIME ZONE,
  last_error_code VARCHAR2(100 CHAR),
  last_error_detail VARCHAR2(4000 CHAR)
);

-- 기존 실행은 보존하되 canonical payload가 없으므로 zero digest로 fail-closed 재사용합니다.
UPDATE CPF_BATCH_EXECUTION_CONTROL
   SET idempotency_scope = job_id || ':' || TO_CHAR(definition_version) || ':' || approval_id,
       request_hash = RPAD('0', 64, '0'),
       plan_checksum = RPAD('0', 64, '0'),
       last_error_code = CASE WHEN control_status IN ('COMPLETED','FAILED','STOPPED','ABANDONED','REJECTED')
                              THEN last_error_code ELSE 'LEGACY_EXECUTION_REQUIRES_RECONCILIATION' END
 WHERE idempotency_scope IS NULL;

ALTER TABLE CPF_BATCH_EXECUTION_CONTROL MODIFY (
  idempotency_scope NOT NULL,
  request_hash NOT NULL,
  plan_checksum NOT NULL
);

DECLARE
  v_constraint VARCHAR2(128);
BEGIN
  SELECT uc.constraint_name INTO v_constraint
    FROM user_constraints uc
   WHERE uc.table_name = 'CPF_BATCH_EXECUTION_CONTROL'
     AND uc.constraint_type = 'U'
     AND (SELECT COUNT(*) FROM user_cons_columns cc
           WHERE cc.constraint_name = uc.constraint_name AND cc.table_name = uc.table_name) = 1
     AND EXISTS (SELECT 1 FROM user_cons_columns cc
                  WHERE cc.constraint_name = uc.constraint_name AND cc.table_name = uc.table_name
                    AND cc.column_name = 'IDEMPOTENCY_KEY');
  EXECUTE IMMEDIATE 'ALTER TABLE CPF_BATCH_EXECUTION_CONTROL DROP CONSTRAINT ' || v_constraint;
EXCEPTION WHEN NO_DATA_FOUND THEN
  RAISE_APPLICATION_ERROR(-20090, 'Legacy global idempotency unique constraint was not found');
END;
/

CREATE UNIQUE INDEX UK_CPF_BAT_EXEC_IDEM_SCOPE
  ON CPF_BATCH_EXECUTION_CONTROL(idempotency_scope, idempotency_key);
CREATE INDEX IX_CPF_BAT_EXEC_RECONCILE
  ON CPF_BATCH_EXECUTION_CONTROL(control_status, reconcile_after, updated_at);
ALTER TABLE CPF_BATCH_EXECUTION_CONTROL ADD (
  CONSTRAINT CK_CPF_BAT_REQUEST_HASH CHECK (REGEXP_LIKE(request_hash, '^[0-9a-f]{64}$')),
  CONSTRAINT CK_CPF_BAT_PLAN_HASH CHECK (REGEXP_LIKE(plan_checksum, '^[0-9a-f]{64}$')),
  CONSTRAINT CK_CPF_BAT_CONTROL_VERSION CHECK (control_version > 0),
  CONSTRAINT CK_CPF_BAT_RECONCILE_ATTEMPT CHECK (reconcile_attempts >= 0)
);

CREATE TABLE CPF_BATCH_EXECUTION_EPOCH (
  job_id VARCHAR2(80 CHAR) PRIMARY KEY,
  current_fencing_token NUMBER(19) NOT NULL,
  epoch_version NUMBER(19) DEFAULT 1 NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT CK_CPF_BAT_EPOCH_TOKEN CHECK (current_fencing_token > 0),
  CONSTRAINT CK_CPF_BAT_EPOCH_VERSION CHECK (epoch_version > 0)
);

INSERT INTO CPF_BATCH_EXECUTION_EPOCH(job_id, current_fencing_token, epoch_version, updated_at)
SELECT job_id, MAX(fencing_token), 1, SYSTIMESTAMP
  FROM CPF_BATCH_EXECUTION_CONTROL
 GROUP BY job_id;
