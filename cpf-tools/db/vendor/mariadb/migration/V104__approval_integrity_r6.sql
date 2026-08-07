-- CPF R6 approval policy immutability/audit extension
-- vendor=mariadb; logicalDatabase=admDB
USE admDB;
CREATE TABLE IF NOT EXISTS adm_approval_policy_history (
    POLICY_HISTORY_ID BIGINT NOT NULL AUTO_INCREMENT,
    POLICY_CODE VARCHAR(80) NOT NULL,
    POLICY_VERSION INT NOT NULL,
    CHANGE_TYPE VARCHAR(30) NOT NULL,
    CHANGE_REASON VARCHAR(500) NOT NULL,
    BEFORE_HASH CHAR(64) NULL,
    AFTER_HASH CHAR(64) NOT NULL,
    OPERATOR_ID VARCHAR(50) NOT NULL,
    CREATED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_policy_history PRIMARY KEY (POLICY_HISTORY_ID),
    CONSTRAINT fk_adm_approval_policy_history_policy FOREIGN KEY (POLICY_CODE, POLICY_VERSION)
      REFERENCES adm_approval_policy (POLICY_CODE, POLICY_VERSION),
    CONSTRAINT ck_adm_approval_policy_history_hash CHECK
      ((BEFORE_HASH IS NULL OR CHAR_LENGTH(BEFORE_HASH)=64) AND CHAR_LENGTH(AFTER_HASH)=64),
    INDEX ix_adm_approval_policy_history_policy (POLICY_CODE, POLICY_VERSION, POLICY_HISTORY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Immutable approval policy version audit history';
