-- CPF R6 approval runtime hardening: stale execution lease, single-use capability nonce, policy immutability/overlap
-- vendor=postgresql; logicalDatabase=admDB
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS LEASE_OWNER VARCHAR(120);
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS LEASE_EXPIRES_AT TIMESTAMP(3);
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS FENCE_TOKEN BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS ix_adm_approval_execution_lease ON adm_approval_execution (EXECUTION_STATUS, LEASE_EXPIRES_AT, APPROVAL_REQUEST_ID);

CREATE TABLE IF NOT EXISTS adm_approval_policy_lock (
  LOCK_BUCKET INTEGER PRIMARY KEY CHECK (LOCK_BUCKET BETWEEN 0 AND 63),
  created_by VARCHAR(50) NOT NULL DEFAULT 'ADM', created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM', updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO adm_approval_policy_lock (LOCK_BUCKET,created_by,updated_by) VALUES
(0,'ADM','ADM'),
(1,'ADM','ADM'),
(2,'ADM','ADM'),
(3,'ADM','ADM'),
(4,'ADM','ADM'),
(5,'ADM','ADM'),
(6,'ADM','ADM'),
(7,'ADM','ADM'),
(8,'ADM','ADM'),
(9,'ADM','ADM'),
(10,'ADM','ADM'),
(11,'ADM','ADM'),
(12,'ADM','ADM'),
(13,'ADM','ADM'),
(14,'ADM','ADM'),
(15,'ADM','ADM'),
(16,'ADM','ADM'),
(17,'ADM','ADM'),
(18,'ADM','ADM'),
(19,'ADM','ADM'),
(20,'ADM','ADM'),
(21,'ADM','ADM'),
(22,'ADM','ADM'),
(23,'ADM','ADM'),
(24,'ADM','ADM'),
(25,'ADM','ADM'),
(26,'ADM','ADM'),
(27,'ADM','ADM'),
(28,'ADM','ADM'),
(29,'ADM','ADM'),
(30,'ADM','ADM'),
(31,'ADM','ADM'),
(32,'ADM','ADM'),
(33,'ADM','ADM'),
(34,'ADM','ADM'),
(35,'ADM','ADM'),
(36,'ADM','ADM'),
(37,'ADM','ADM'),
(38,'ADM','ADM'),
(39,'ADM','ADM'),
(40,'ADM','ADM'),
(41,'ADM','ADM'),
(42,'ADM','ADM'),
(43,'ADM','ADM'),
(44,'ADM','ADM'),
(45,'ADM','ADM'),
(46,'ADM','ADM'),
(47,'ADM','ADM'),
(48,'ADM','ADM'),
(49,'ADM','ADM'),
(50,'ADM','ADM'),
(51,'ADM','ADM'),
(52,'ADM','ADM'),
(53,'ADM','ADM'),
(54,'ADM','ADM'),
(55,'ADM','ADM'),
(56,'ADM','ADM'),
(57,'ADM','ADM'),
(58,'ADM','ADM'),
(59,'ADM','ADM'),
(60,'ADM','ADM'),
(61,'ADM','ADM'),
(62,'ADM','ADM'),
(63,'ADM','ADM')
ON CONFLICT (LOCK_BUCKET) DO NOTHING;

CREATE TABLE IF NOT EXISTS adm_approval_capability_nonce (
  NONCE_HASH CHAR(64) PRIMARY KEY,
  APPROVAL_REFERENCE VARCHAR(240) NOT NULL,
  EXPIRES_AT TIMESTAMP(3) NOT NULL,
  CONSUMED_AT TIMESTAMP(3),
  CONSUMED_BY VARCHAR(80),
  created_by VARCHAR(50) NOT NULL DEFAULT 'ADM',
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM',
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_adm_approval_cap_nonce_hash CHECK (CHAR_LENGTH(NONCE_HASH)=64)
);
CREATE INDEX IF NOT EXISTS ix_adm_approval_cap_nonce_expiry ON adm_approval_capability_nonce (EXPIRES_AT, CONSUMED_AT);

CREATE OR REPLACE FUNCTION cpf_adm_approval_policy_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION 'adm_approval_policy versions are immutable; insert a new version';
END $$;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_u ON adm_approval_policy;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_d ON adm_approval_policy;
CREATE TRIGGER tr_adm_approval_policy_immutable_u BEFORE UPDATE ON adm_approval_policy FOR EACH ROW EXECUTE FUNCTION cpf_adm_approval_policy_immutable();
CREATE TRIGGER tr_adm_approval_policy_immutable_d BEFORE DELETE ON adm_approval_policy FOR EACH ROW EXECUTE FUNCTION cpf_adm_approval_policy_immutable();

CREATE OR REPLACE FUNCTION cpf_adm_approval_policy_no_overlap() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_lock INTEGER;
BEGIN
  IF NEW.ENABLED_YN='Y' THEN
    -- DB-level serialization: direct SQL and application callers share one global policy insert lock.
    SELECT LOCK_BUCKET INTO v_lock FROM adm_approval_policy_lock WHERE LOCK_BUCKET=0 FOR UPDATE;
    IF EXISTS (
      SELECT 1 FROM adm_approval_policy p
       WHERE p.ACTION_TYPE=NEW.ACTION_TYPE AND p.ENABLED_YN='Y'
         AND p.EFFECTIVE_FROM < COALESCE(NEW.EFFECTIVE_TO, TIMESTAMP '9999-12-31 23:59:59')
         AND NEW.EFFECTIVE_FROM < COALESCE(p.EFFECTIVE_TO, TIMESTAMP '9999-12-31 23:59:59')
    ) THEN
      RAISE EXCEPTION 'active approval policy range overlaps actionType=%', NEW.ACTION_TYPE;
    END IF;
  END IF;
  RETURN NEW;
END $$;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_no_overlap ON adm_approval_policy;
CREATE TRIGGER tr_adm_approval_policy_no_overlap BEFORE INSERT ON adm_approval_policy FOR EACH ROW EXECUTE FUNCTION cpf_adm_approval_policy_no_overlap();
