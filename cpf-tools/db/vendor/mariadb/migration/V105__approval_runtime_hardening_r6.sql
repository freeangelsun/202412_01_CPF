-- CPF R6 approval runtime hardening: stale execution lease, single-use capability nonce, policy immutability/overlap
-- vendor=mariadb; logicalDatabase=admDB
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS LEASE_OWNER VARCHAR(120) NULL;
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS LEASE_EXPIRES_AT DATETIME(3) NULL;
ALTER TABLE adm_approval_execution ADD COLUMN IF NOT EXISTS FENCE_TOKEN BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS ix_adm_approval_execution_lease ON adm_approval_execution (EXECUTION_STATUS, LEASE_EXPIRES_AT, APPROVAL_REQUEST_ID);

CREATE TABLE IF NOT EXISTS adm_approval_policy_lock (
  LOCK_BUCKET INT NOT NULL,
  created_by VARCHAR(50) NOT NULL DEFAULT 'ADM', created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM', updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (LOCK_BUCKET), CONSTRAINT ck_adm_approval_policy_lock_bucket CHECK (LOCK_BUCKET BETWEEN 0 AND 63)
);
INSERT IGNORE INTO adm_approval_policy_lock (LOCK_BUCKET,created_by,updated_by) VALUES
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
(63,'ADM','ADM');

CREATE TABLE IF NOT EXISTS adm_approval_capability_nonce (
  NONCE_HASH CHAR(64) NOT NULL,
  APPROVAL_REFERENCE VARCHAR(240) NOT NULL,
  EXPIRES_AT DATETIME(3) NOT NULL,
  CONSUMED_AT DATETIME(3) NULL,
  CONSUMED_BY VARCHAR(80) NULL,
  created_by VARCHAR(50) NOT NULL DEFAULT 'ADM',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (NONCE_HASH),
  CONSTRAINT ck_adm_approval_cap_nonce_hash CHECK (CHAR_LENGTH(NONCE_HASH)=64),
  KEY ix_adm_approval_cap_nonce_expiry (EXPIRES_AT, CONSUMED_AT)
);

DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_u;
DROP TRIGGER IF EXISTS tr_adm_approval_policy_immutable_d;
CREATE TRIGGER tr_adm_approval_policy_immutable_u BEFORE UPDATE ON adm_approval_policy FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='adm_approval_policy versions are immutable; insert a new version';
CREATE TRIGGER tr_adm_approval_policy_immutable_d BEFORE DELETE ON adm_approval_policy FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='adm_approval_policy versions are immutable; insert a new version';

DELIMITER $$
DROP TRIGGER IF EXISTS tr_adm_approval_policy_no_overlap$$
CREATE TRIGGER tr_adm_approval_policy_no_overlap BEFORE INSERT ON adm_approval_policy FOR EACH ROW
BEGIN
  DECLARE v_lock INT;
  IF NEW.ENABLED_YN='Y' THEN
    -- DB-level serialization closes concurrent direct-SQL overlap races.
    SELECT LOCK_BUCKET INTO v_lock FROM adm_approval_policy_lock WHERE LOCK_BUCKET=0 FOR UPDATE;
    IF EXISTS (
      SELECT 1 FROM adm_approval_policy p
       WHERE p.ACTION_TYPE=NEW.ACTION_TYPE AND p.ENABLED_YN='Y'
         AND p.EFFECTIVE_FROM < COALESCE(NEW.EFFECTIVE_TO, TIMESTAMP('9999-12-31 23:59:59'))
         AND NEW.EFFECTIVE_FROM < COALESCE(p.EFFECTIVE_TO, TIMESTAMP('9999-12-31 23:59:59'))
    ) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='active approval policy effective range overlaps';
    END IF;
  END IF;
END$$
DELIMITER ;
