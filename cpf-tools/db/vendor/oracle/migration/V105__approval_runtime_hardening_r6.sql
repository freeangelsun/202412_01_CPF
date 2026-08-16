-- CPF R6 approval runtime hardening: stale execution lease, single-use capability nonce, policy immutability/overlap
-- vendor=oracle; logicalDatabase=admDB
BEGIN EXECUTE IMMEDIATE 'ALTER TABLE adm_approval_execution ADD (LEASE_OWNER VARCHAR2(120 CHAR))'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -1430 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'ALTER TABLE adm_approval_execution ADD (LEASE_EXPIRES_AT TIMESTAMP(3))'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -1430 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'ALTER TABLE adm_approval_execution ADD (FENCE_TOKEN NUMBER(19) DEFAULT 0 NOT NULL)'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -1430 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'CREATE INDEX ix_adm_approval_execution_lease ON adm_approval_execution (EXECUTION_STATUS, LEASE_EXPIRES_AT, APPROVAL_REQUEST_ID)'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
BEGIN
  EXECUTE IMMEDIATE q'[CREATE TABLE adm_approval_policy_lock (
    LOCK_BUCKET NUMBER(10) NOT NULL, created_by VARCHAR2(50 CHAR) DEFAULT 'ADM' NOT NULL,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_by VARCHAR2(50 CHAR) DEFAULT 'ADM' NOT NULL,
    updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_adm_approval_policy_lock PRIMARY KEY (LOCK_BUCKET),
    CONSTRAINT ck_adm_approval_policy_lock_bucket CHECK (LOCK_BUCKET BETWEEN 0 AND 63))]';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
BEGIN
  FOR i IN 0..63 LOOP
    BEGIN INSERT INTO adm_approval_policy_lock (LOCK_BUCKET,created_by,updated_by) VALUES (i,'ADM','ADM');
    EXCEPTION WHEN DUP_VAL_ON_INDEX THEN NULL; END;
  END LOOP;
END;
/

BEGIN
  EXECUTE IMMEDIATE q'[CREATE TABLE adm_approval_capability_nonce (
    NONCE_HASH CHAR(64 CHAR) NOT NULL, APPROVAL_REFERENCE VARCHAR2(240 CHAR) NOT NULL,
    EXPIRES_AT TIMESTAMP(3) NOT NULL, CONSUMED_AT TIMESTAMP(3), CONSUMED_BY VARCHAR2(80 CHAR),
    created_by VARCHAR2(50 CHAR) DEFAULT 'ADM' NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR2(50 CHAR) DEFAULT 'ADM' NOT NULL, updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_adm_approval_cap_nonce PRIMARY KEY (NONCE_HASH),
    CONSTRAINT ck_adm_approval_cap_nonce_hash CHECK (LENGTH(NONCE_HASH)=64))]';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'CREATE INDEX ix_adm_approval_cap_nonce_expiry ON adm_approval_capability_nonce (EXPIRES_AT, CONSUMED_AT)'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
CREATE OR REPLACE TRIGGER tr_adm_approval_policy_immutable_u BEFORE UPDATE ON adm_approval_policy FOR EACH ROW
BEGIN RAISE_APPLICATION_ERROR(-20061,'adm_approval_policy versions are immutable; insert a new version'); END;
/
CREATE OR REPLACE TRIGGER tr_adm_approval_policy_immutable_d BEFORE DELETE ON adm_approval_policy FOR EACH ROW
BEGIN RAISE_APPLICATION_ERROR(-20062,'adm_approval_policy versions are immutable; insert a new version'); END;
/
CREATE OR REPLACE TRIGGER tr_adm_approval_policy_no_overlap
FOR INSERT ON adm_approval_policy
COMPOUND TRIGGER
  TYPE t_policy_row IS RECORD (
    policy_code adm_approval_policy.POLICY_CODE%TYPE,
    policy_version adm_approval_policy.POLICY_VERSION%TYPE,
    action_type adm_approval_policy.ACTION_TYPE%TYPE,
    effective_from adm_approval_policy.EFFECTIVE_FROM%TYPE,
    effective_to adm_approval_policy.EFFECTIVE_TO%TYPE,
    enabled_yn adm_approval_policy.ENABLED_YN%TYPE
  );
  TYPE t_policy_rows IS TABLE OF t_policy_row INDEX BY PLS_INTEGER;
  g_rows t_policy_rows;
  g_count PLS_INTEGER := 0;

  BEFORE EACH ROW IS
    v_lock NUMBER;
  BEGIN
    IF :NEW.ENABLED_YN='Y' THEN
      -- Lock a non-mutating guard row before statement completion so concurrent direct SQL cannot race.
      SELECT LOCK_BUCKET INTO v_lock FROM adm_approval_policy_lock WHERE LOCK_BUCKET=0 FOR UPDATE;
      g_count := g_count + 1;
      g_rows(g_count).policy_code := :NEW.POLICY_CODE;
      g_rows(g_count).policy_version := :NEW.POLICY_VERSION;
      g_rows(g_count).action_type := :NEW.ACTION_TYPE;
      g_rows(g_count).effective_from := :NEW.EFFECTIVE_FROM;
      g_rows(g_count).effective_to := :NEW.EFFECTIVE_TO;
      g_rows(g_count).enabled_yn := :NEW.ENABLED_YN;
    END IF;
  END BEFORE EACH ROW;

  AFTER STATEMENT IS
    v_count NUMBER;
  BEGIN
    FOR i IN 1..g_count LOOP
      SELECT COUNT(*) INTO v_count FROM adm_approval_policy p
       WHERE p.ACTION_TYPE=g_rows(i).action_type AND p.ENABLED_YN='Y'
         AND p.EFFECTIVE_FROM < NVL(g_rows(i).effective_to, TIMESTAMP '9999-12-31 23:59:59')
         AND g_rows(i).effective_from < NVL(p.EFFECTIVE_TO, TIMESTAMP '9999-12-31 23:59:59')
         AND NOT (p.POLICY_CODE=g_rows(i).policy_code AND p.POLICY_VERSION=g_rows(i).policy_version);
      IF v_count>0 THEN
        RAISE_APPLICATION_ERROR(-20063,'active approval policy effective range overlaps');
      END IF;
    END LOOP;
  END AFTER STATEMENT;
END;
/
