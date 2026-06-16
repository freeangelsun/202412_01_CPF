-- CPF all-in-one install and smoke-check script
-- Generated from split SQL files. Execute this file directly from any working directory.

-- ============================================================================
-- BEGIN specs/sql/01_create_databases.sql
-- ============================================================================
-- CPF initial database creation for MariaDB/MySQL.
-- Run with a user that can create databases.

CREATE DATABASE IF NOT EXISTS pfwDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS admDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS accDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mbrDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- END specs/sql/01_create_databases.sql

-- ============================================================================
-- BEGIN specs/sql/02_create_service_users.sql
-- ============================================================================
-- Local least-privilege service users for CPF modules.
-- These accounts are for local/test only. Change passwords or use environment variables in real deployments.

CREATE USER IF NOT EXISTS 'cpf_pfw'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr'@'localhost' IDENTIFIED BY 'cpf_local_pw';

GRANT SELECT, INSERT, UPDATE, DELETE ON pfwDB.* TO 'cpf_pfw'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON admDB.* TO 'cpf_adm'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON accDB.* TO 'cpf_acc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON mbrDB.* TO 'cpf_mbr'@'localhost';

FLUSH PRIVILEGES;

-- END specs/sql/02_create_service_users.sql

-- ============================================================================
-- BEGIN specs/sql/03_cleanup_legacy_layout.sql
-- ============================================================================
-- Cleanup legacy local/test sample layout.
-- Earlier CPF sample scripts created CMN-owned reference tables and member samples outside their domains.
-- The current layout owns framework reference tables in pfwDB and MBR samples in mbrDB.member only.

DROP DATABASE IF EXISTS cmnDB;
DROP TABLE IF EXISTS admDB.member;
DROP TABLE IF EXISTS accDB.acc_member;

-- END specs/sql/03_cleanup_legacy_layout.sql

-- ============================================================================
-- BEGIN specs/sql/10_pfw_schema.sql
-- ============================================================================
-- PFW framework schema.
-- Target DB: pfwDB

USE pfwDB;

CREATE TABLE IF NOT EXISTS TRAN_LOG (
    LOG_DATE DATE NOT NULL,
    LOG_IDX BIGINT NOT NULL AUTO_INCREMENT,
    TRANSACTION_ID VARCHAR(100) NULL,
    TRACE_ID VARCHAR(100) NULL,
    SPAN_ID VARCHAR(100) NULL,
    PARENT_SPAN_ID VARCHAR(100) NULL,
    SEQUENCE_NO INT NULL DEFAULT 1,
    MODULE_ID VARCHAR(20) NULL DEFAULT 'N/A',
    MENU_ID VARCHAR(50) NULL,
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL,
    BUSINESS_TRANSACTION_NAME VARCHAR(150) NULL,
    LOG_TYPE VARCHAR(20) NULL DEFAULT 'N/A',
    REQUEST_TYPE VARCHAR(20) NULL,
    ORIGINAL_CHANNEL_CODE VARCHAR(20) NULL,
    CHANNEL_CODE VARCHAR(20) NULL,
    MEMBER_NO VARCHAR(50) NULL,
    CUSTOMER_NO VARCHAR(50) NULL,
    SCREEN_ID VARCHAR(50) NULL,
    DEVICE_ID VARCHAR(100) NULL,
    CLIENT_REQUEST_TIME VARCHAR(30) NULL,
    WAS_ID VARCHAR(50) NULL,
    RESERVED_FIELD_1 VARCHAR(255) NULL,
    RESERVED_FIELD_2 VARCHAR(255) NULL,
    RESERVED_FIELD_3 VARCHAR(255) NULL,
    RESERVED_FIELD_4 VARCHAR(255) NULL,
    RESERVED_FIELD_5 VARCHAR(255) NULL,
    HTTP_METHOD VARCHAR(10) NULL,
    URI VARCHAR(500) NULL DEFAULT 'N/A',
    CONTROLLER VARCHAR(255) NULL,
    EXECUTION_PACKAGE VARCHAR(255) NULL,
    EXECUTION_CLASS VARCHAR(255) NULL,
    EXECUTION_METHOD VARCHAR(100) NULL,
    EXECUTION_SIGNATURE VARCHAR(1000) NULL,
    WORKFLOW_ID VARCHAR(50) NULL,
    WORKFLOW_NAME VARCHAR(100) NULL,
    WORKFLOW_INSTANCE_ID VARCHAR(100) NULL,
    WORKFLOW_STEP_ID VARCHAR(50) NULL,
    WORKFLOW_STEP_NAME VARCHAR(100) NULL,
    WORKFLOW_STATUS VARCHAR(30) NULL,
    WORKFLOW_FAILURE_POLICY VARCHAR(30) NULL,
    COMPENSATION_YN CHAR(1) NOT NULL DEFAULT 'N',
    COMPENSATION_TRANSACTION_ID VARCHAR(20) NULL,
    COMPENSATION_TARGET_TRANSACTION_ID VARCHAR(20) NULL,
    COMPENSATION_STATUS VARCHAR(30) NULL,
    PARAMETERS MEDIUMTEXT NULL,
    REQUEST_BODY MEDIUMTEXT NULL,
    RESPONSE MEDIUMTEXT NULL,
    HTTP_STATUS INT NULL,
    RESPONSE_CODE VARCHAR(20) NULL,
    MESSAGE_CODE VARCHAR(20) NULL,
    MESSAGE_CONTENT VARCHAR(1000) NULL,
    ERROR_MESSAGE MEDIUMTEXT NULL,
    ERROR_CODE VARCHAR(100) NULL,
    EXTERNAL_MESSAGE VARCHAR(1000) NULL,
    INTERNAL_MESSAGE MEDIUMTEXT NULL,
    EXEC_USER VARCHAR(100) NOT NULL DEFAULT 'N/A',
    CLIENT_IP VARCHAR(100) NULL,
    USER_AGENT VARCHAR(500) NULL,
    START_TIME DATETIME(3) NULL,
    END_TIME DATETIME(3) NULL,
    DURATION_MS BIGINT NULL,
    CREATED_BY VARCHAR(100) NOT NULL DEFAULT 'PFW',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(100) NOT NULL DEFAULT 'PFW',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (LOG_IDX),
    INDEX IDX_TRAN_LOG_DATE (LOG_DATE),
    INDEX IDX_TRAN_LOG_TRANSACTION_ID (TRANSACTION_ID),
    INDEX IDX_TRAN_LOG_TRANSACTION_TIME (TRANSACTION_ID, START_TIME, LOG_IDX),
    INDEX IDX_TRAN_LOG_TRACE_ID (TRACE_ID),
    INDEX IDX_TRAN_LOG_BUSINESS_TRANSACTION_ID (BUSINESS_TRANSACTION_ID),
    INDEX IDX_TRAN_LOG_BUSINESS_TIME (BUSINESS_TRANSACTION_ID, START_TIME),
    INDEX IDX_TRAN_LOG_MEMBER_TIME (MEMBER_NO, START_TIME),
    INDEX IDX_TRAN_LOG_CUSTOMER_TIME (CUSTOMER_NO, START_TIME),
    INDEX IDX_TRAN_LOG_CHANNEL_TIME (CHANNEL_CODE, START_TIME),
    INDEX IDX_TRAN_LOG_MODULE_TIME (MODULE_ID, START_TIME),
    INDEX IDX_TRAN_LOG_STATUS_TIME (LOG_TYPE, RESPONSE_CODE, START_TIME),
    INDEX IDX_TRAN_LOG_HTTP_STATUS_TIME (HTTP_STATUS, START_TIME),
    INDEX IDX_TRAN_LOG_MESSAGE_CODE (MESSAGE_CODE, START_TIME),
    INDEX IDX_TRAN_LOG_ERROR_CODE (ERROR_CODE, START_TIME),
    INDEX IDX_TRAN_LOG_WORKFLOW_TIME (WORKFLOW_INSTANCE_ID, START_TIME, LOG_IDX),
    INDEX IDX_TRAN_LOG_COMPENSATION (COMPENSATION_YN, COMPENSATION_STATUS),
    INDEX IDX_TRAN_LOG_EXECUTION_TIME (EXECUTION_CLASS, EXECUTION_METHOD, START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS TRAN_LOG_DTL (
    DETAIL_ID BIGINT NOT NULL AUTO_INCREMENT,
    LOG_IDX BIGINT NOT NULL,
    DETAIL_KEY VARCHAR(100) NOT NULL DEFAULT 'N/A',
    DETAIL_VALUE MEDIUMTEXT NOT NULL,
    CREATED_BY VARCHAR(100) NOT NULL DEFAULT 'PFW',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(100) NOT NULL DEFAULT 'PFW',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (DETAIL_ID),
    CONSTRAINT FK_TRAN_LOG_DTL_LOG
        FOREIGN KEY (LOG_IDX) REFERENCES TRAN_LOG(LOG_IDX)
        ON DELETE CASCADE,
    INDEX IDX_TRAN_LOG_DTL_LOG_IDX (LOG_IDX),
    INDEX IDX_TRAN_LOG_DTL_LOG_KEY (LOG_IDX, DETAIL_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS code_table (
    code_id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NULL,
    code_key VARCHAR(80) NOT NULL,
    code_value VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (code_id),
    CONSTRAINT FK_CODE_PARENT
        FOREIGN KEY (parent_id) REFERENCES code_table(code_id)
        ON DELETE SET NULL,
    UNIQUE KEY UK_CODE_KEY_VALUE (code_key, code_value),
    INDEX IDX_CODE_PARENT (parent_id),
    INDEX IDX_CODE_USE (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS message_table (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    message_code VARCHAR(20) NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'ko',
    message_format_type VARCHAR(20) NOT NULL DEFAULT 'FIXED',
    external_message VARCHAR(2000) NOT NULL,
    internal_message VARCHAR(4000) NOT NULL,
    parameter_count INT NOT NULL DEFAULT 0,
    parameter_sample VARCHAR(1000) NULL,
    description VARCHAR(500) NULL,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    UNIQUE KEY UK_MESSAGE_CODE_LOCALE (message_code, locale),
    INDEX IDX_MESSAGE_CODE_USE (message_code, use_yn),
    INDEX IDX_MESSAGE_USE (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS response_code_table (
    response_code VARCHAR(20) NOT NULL,
    message_code VARCHAR(20) NOT NULL,
    result_type CHAR(1) NOT NULL,
    module_id VARCHAR(3) NOT NULL,
    response_group VARCHAR(2) NOT NULL,
    sequence_no VARCHAR(4) NOT NULL,
    http_status INT NOT NULL,
    description VARCHAR(500) NULL,
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (response_code),
    INDEX IDX_RESPONSE_CODE_MESSAGE (message_code),
    INDEX IDX_RESPONSE_CODE_MODULE (module_id, result_type, response_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS config_table (
    config_id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(150) NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    config_type VARCHAR(30) NOT NULL DEFAULT 'STRING',
    description VARCHAR(500) NULL,
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id),
    UNIQUE KEY UK_CONFIG_KEY (config_key),
    INDEX IDX_CONFIG_USE (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cache_refresh_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    cache_name VARCHAR(50) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    event_key VARCHAR(200) NULL,
    source_was_id VARCHAR(50) NULL,
    published_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    published_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    INDEX IDX_CACHE_REFRESH_EVENT_CACHE_ID (cache_name, event_id),
    INDEX IDX_CACHE_REFRESH_EVENT_TIME (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_exchange_log (
    EXCHANGE_ID VARCHAR(80) NOT NULL,
    TRANSACTION_ID VARCHAR(80) NULL,
    TRACE_ID VARCHAR(80) NULL,
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL,
    ACTION_TYPE VARCHAR(30) NOT NULL,
    PROTOCOL VARCHAR(20) NOT NULL,
    DIRECTION VARCHAR(20) NULL,
    EXECUTED_YN CHAR(1) NOT NULL DEFAULT 'N',
    SUCCESS_YN CHAR(1) NOT NULL DEFAULT 'N',
    HOST VARCHAR(255) NULL,
    SOURCE_PATH VARCHAR(1000) NULL,
    TARGET_PATH VARCHAR(1000) NULL,
    REQUEST_USER VARCHAR(50) NULL,
    MESSAGE VARCHAR(2000) NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (EXCHANGE_ID),
    INDEX IX_FILE_EXCHANGE_TX (TRANSACTION_ID, CREATED_AT),
    INDEX IX_FILE_EXCHANGE_BIZ (BUSINESS_TRANSACTION_ID, CREATED_AT),
    INDEX IX_FILE_EXCHANGE_HOST (HOST, CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS security_jwt_key (
    KEY_ID VARCHAR(80) NOT NULL,
    ISSUER VARCHAR(100) NOT NULL,
    ALGORITHM VARCHAR(20) NOT NULL DEFAULT 'HS256',
    SECRET_REF VARCHAR(500) NOT NULL,
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'Y',
    EXPIRE_AT DATETIME NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (KEY_ID),
    INDEX IX_SECURITY_JWT_KEY_ISSUER (ISSUER, ACTIVE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS security_token_audit_log (
    TOKEN_AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT,
    TRANSACTION_ID VARCHAR(80) NULL,
    TRACE_ID VARCHAR(80) NULL,
    TOKEN_HASH VARCHAR(512) NULL,
    TOKEN_TYPE VARCHAR(30) NOT NULL DEFAULT 'Bearer',
    ISSUER VARCHAR(100) NULL,
    SUBJECT VARCHAR(200) NULL,
    AUDIENCE VARCHAR(200) NULL,
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'N',
    EXPIRE_AT DATETIME NULL,
    FAILURE_REASON VARCHAR(1000) NULL,
    CLIENT_IP VARCHAR(50) NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'PFW',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (TOKEN_AUDIT_ID),
    INDEX IX_SECURITY_TOKEN_TX (TRANSACTION_ID),
    INDEX IX_SECURITY_TOKEN_HASH (TOKEN_HASH),
    INDEX IX_SECURITY_TOKEN_SUBJECT_TIME (SUBJECT, CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- END specs/sql/10_pfw_schema.sql

-- ============================================================================
-- BEGIN specs/sql/20_cmn_schema.sql
-- ============================================================================
-- CMN has no standalone schema in the current CPF layout.
-- Framework/common reference data is owned by pfwDB.

-- END specs/sql/20_cmn_schema.sql

-- ============================================================================
-- BEGIN specs/sql/30_adm_schema.sql
-- ============================================================================
-- ADM administration schema.
-- Target DB: admDB

USE admDB;

CREATE TABLE IF NOT EXISTS operator_user (
    OPERATOR_ID VARCHAR(50) NOT NULL,
    OPERATOR_NAME VARCHAR(100) NOT NULL,
    PASSWORD_HASH VARCHAR(512) NOT NULL,
    LOCKED_YN CHAR(1) NOT NULL DEFAULT 'N',
    FAIL_COUNT INT NOT NULL DEFAULT 0,
    PASSWORD_CHANGED_AT DATETIME NULL,
    PASSWORD_CHANGE_REQUIRED_YN CHAR(1) NOT NULL DEFAULT 'Y',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y',
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (OPERATOR_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_role (
    ROLE_ID VARCHAR(50) NOT NULL,
    ROLE_NAME VARCHAR(100) NOT NULL,
    DESCRIPTION VARCHAR(500) NULL,
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y',
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ROLE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_user_role (
    OPERATOR_ID VARCHAR(50) NOT NULL,
    ROLE_ID VARCHAR(50) NOT NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (OPERATOR_ID, ROLE_ID),
    CONSTRAINT FK_OPERATOR_USER_ROLE_USER
        FOREIGN KEY (OPERATOR_ID) REFERENCES operator_user(OPERATOR_ID)
        ON DELETE CASCADE,
    CONSTRAINT FK_OPERATOR_USER_ROLE_ROLE
        FOREIGN KEY (ROLE_ID) REFERENCES operator_role(ROLE_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_menu (
    MENU_ID VARCHAR(50) NOT NULL,
    PARENT_MENU_ID VARCHAR(50) NULL,
    MENU_NAME VARCHAR(100) NOT NULL,
    MENU_PATH VARCHAR(200) NOT NULL,
    SORT_ORDER INT NOT NULL DEFAULT 0,
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y',
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (MENU_ID),
    INDEX IX_OPERATOR_MENU_PARENT (PARENT_MENU_ID, SORT_ORDER),
    CONSTRAINT FK_OPERATOR_MENU_PARENT
        FOREIGN KEY (PARENT_MENU_ID) REFERENCES operator_menu(MENU_ID)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_role_menu (
    ROLE_ID VARCHAR(50) NOT NULL,
    MENU_ID VARCHAR(50) NOT NULL,
    READ_YN CHAR(1) NOT NULL DEFAULT 'Y',
    WRITE_YN CHAR(1) NOT NULL DEFAULT 'N',
    DELETE_YN CHAR(1) NOT NULL DEFAULT 'N',
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ROLE_ID, MENU_ID),
    CONSTRAINT FK_OPERATOR_ROLE_MENU_ROLE
        FOREIGN KEY (ROLE_ID) REFERENCES operator_role(ROLE_ID)
        ON DELETE CASCADE,
    CONSTRAINT FK_OPERATOR_ROLE_MENU_MENU
        FOREIGN KEY (MENU_ID) REFERENCES operator_menu(MENU_ID)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_audit_log (
    AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT,
    TRANSACTION_ID VARCHAR(80) NULL,
    TRACE_ID VARCHAR(80) NULL,
    OPERATOR_ID VARCHAR(50) NOT NULL,
    MENU_ID VARCHAR(50) NULL,
    ACTION_TYPE VARCHAR(30) NOT NULL,
    TARGET_TYPE VARCHAR(50) NULL,
    TARGET_ID VARCHAR(100) NULL,
    REASON VARCHAR(500) NULL,
    REQUEST_BODY LONGTEXT NULL,
    CLIENT_IP VARCHAR(50) NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (AUDIT_ID),
    INDEX IX_OPERATOR_AUDIT_TX (TRANSACTION_ID),
    INDEX IX_OPERATOR_AUDIT_OPERATOR_TIME (OPERATOR_ID, CREATED_AT),
    INDEX IX_OPERATOR_AUDIT_ACTION_TIME (ACTION_TYPE, CREATED_AT),
    INDEX IX_OPERATOR_AUDIT_TARGET_TIME (TARGET_TYPE, TARGET_ID, CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operator_session (
    SESSION_ID VARCHAR(80) NOT NULL,
    TOKEN_HASH VARCHAR(512) NOT NULL,
    OPERATOR_ID VARCHAR(50) NOT NULL,
    ROLE_IDS VARCHAR(1000) NULL,
    ISSUED_AT DATETIME NOT NULL,
    EXPIRE_AT DATETIME NOT NULL,
    REVOKED_YN CHAR(1) NOT NULL DEFAULT 'N',
    CLIENT_IP VARCHAR(50) NULL,
    USER_AGENT VARCHAR(500) NULL,
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (SESSION_ID),
    INDEX IX_OPERATOR_SESSION_TOKEN (TOKEN_HASH),
    INDEX IX_OPERATOR_SESSION_USER (OPERATOR_ID, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dynamic_log_level_rule (
    RULE_ID VARCHAR(80) NOT NULL,
    TRANSACTION_ID VARCHAR(100) NULL,
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL,
    MODULE_ID VARCHAR(10) NULL,
    LOG_LEVEL VARCHAR(10) NOT NULL,
    EXPIRE_AT DATETIME NOT NULL,
    REASON VARCHAR(500) NULL,
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y',
    CREATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY VARCHAR(50) NOT NULL DEFAULT 'ADM',
    UPDATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (RULE_ID),
    INDEX IX_DYNAMIC_LOG_BIZ_TX (BUSINESS_TRANSACTION_ID, EXPIRE_AT),
    INDEX IX_DYNAMIC_LOG_TX (TRANSACTION_ID, EXPIRE_AT),
    INDEX IX_DYNAMIC_LOG_ACTIVE (USE_YN, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- END specs/sql/30_adm_schema.sql

-- ============================================================================
-- BEGIN specs/sql/40_business_sample_schema.sql
-- ============================================================================
-- Business and education sample schemas used by current code.

USE accDB;

CREATE TABLE IF NOT EXISTS acc_account (
    account_id INT NOT NULL AUTO_INCREMENT,
    account_no VARCHAR(30) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    description TEXT NULL,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id),
    UNIQUE KEY UK_ACC_ACCOUNT_NO (account_no),
    INDEX IDX_ACC_ACCOUNT_STATUS (account_status),
    INDEX IDX_ACC_ACCOUNT_NAME (account_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE mbrDB;

CREATE TABLE IF NOT EXISTS member (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX IDX_MEMBER_NAME (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- END specs/sql/40_business_sample_schema.sql

-- ============================================================================
-- BEGIN specs/sql/50_framework_seed_data.sql
-- ============================================================================
-- CPF framework initial code, response-code, message, and config data.
-- Target DB: pfwDB

USE pfwDB;

INSERT INTO code_table (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    (NULL, 'CODE_GROUP', 'MODULE', 'Service module code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', 'Request type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', 'Channel code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', 'Response result type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', 'Message format type code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', 'Runtime log level code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', 'CMN cache name code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'WORKFLOW_STATUS', 'Workflow status code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'WORKFLOW_FAILURE_POLICY', 'Workflow failure policy code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'FILE_PROTOCOL', 'File exchange protocol code group', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', 'Yes or no code group', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO code_table (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'PFW', 'Framework common library', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', 'Common development library', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ACC', 'Account sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBR', 'Member sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'XYZ', 'Education sample service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', 'Admin service', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', 'Normal request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', 'Compensation request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', 'Retry request', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', 'Web channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', 'Mobile channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', 'Batch channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', 'Admin channel', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', 'Success response', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', 'Error response', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', 'Fixed message without parameters', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', 'Indexed parameter message using {0}, {1}', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'Trace logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'Debug logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'Info logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'Warning logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'Error logging', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', 'Common code cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', 'Common message cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', 'Common response code cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', 'Common config cache', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', 'All common caches', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'STARTED', 'Workflow started', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'SUCCESS', 'Workflow succeeded', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_STATUS') p), 'WORKFLOW_STATUS', 'FAILED', 'Workflow failed', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'ROLLBACK', 'Rollback on failure', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'VERIFY', 'Manual or automatic verification required', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'MANUAL', 'Manual follow-up required', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'WORKFLOW_FAILURE_POLICY') p), 'WORKFLOW_FAILURE_POLICY', 'IGNORE', 'Ignore failure', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'LOCAL', 'Local file protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'FTP', 'FTP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SFTP', 'SFTP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SCP', 'SCP protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'FILE_PROTOCOL') p), 'FILE_PROTOCOL', 'SSH', 'SSH command protocol', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', 'Yes', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM code_table WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', 'No', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO message_table (
    message_code, locale, message_format_type, external_message, internal_message,
    parameter_count, parameter_sample, description, created_by, updated_by
) VALUES
    ('MPFW900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'PFW 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW header validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900001', 'en', 'INDEXED', 'Required transaction header is missing.', 'PFW transaction header validation failed. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW header validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'PFW @FpsTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["MBR01BSE0001"]', 'PFW metadata validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'PFW 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["mbr"]', 'PFW endpoint message', 'SYSTEM', 'SYSTEM'),
    ('MPFW900004', 'ko', 'INDEXED', '동적 로그레벨 설정 요청이 올바르지 않습니다.', 'PFW 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'PFW dynamic log message', 'SYSTEM', 'SYSTEM'),
    ('MPFW990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'PFW 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'PFW internal error message', 'SYSTEM', 'SYSTEM'),
    ('MPFW990001', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'PFW database message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["memberId","abc"]', 'PFW invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["member"]', 'PFW not found message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["memberId"]', 'PFW duplicate message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'PFW validation message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'PFW unauthorized message', 'SYSTEM', 'SYSTEM'),
    ('MPFW010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'PFW forbidden message', 'SYSTEM', 'SYSTEM'),
    ('MPFW020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'PFW business rule message', 'SYSTEM', 'SYSTEM'),
    ('MPFW030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["mbr"]', 'PFW external service message', 'SYSTEM', 'SYSTEM'),
    ('MPFW000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'PFW 공통 요청이 정상 처리되었습니다.', 0, NULL, 'PFW common success message', 'SYSTEM', 'SYSTEM'),
    ('MACC000000', 'ko', 'FIXED', '성공', 'ACC 요청이 정상 처리되었습니다.', 0, NULL, 'ACC success message', 'SYSTEM', 'SYSTEM'),
    ('MACC010000', 'ko', 'FIXED', '성공', 'ACC 업무 요청이 정상 처리되었습니다.', 0, NULL, 'ACC business success message', 'SYSTEM', 'SYSTEM'),
    ('MACC010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', 'ACC 파라미터 검증에 실패했습니다. field={0}', 1, '["accountId"]', 'ACC invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MACC010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', 'ACC 조회 대상이 없습니다. target={0}', 1, '["account"]', 'ACC not found message', 'SYSTEM', 'SYSTEM'),
    ('MMBR000000', 'ko', 'FIXED', '성공', 'MBR 요청이 정상 처리되었습니다.', 0, NULL, 'MBR success message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010001', 'ko', 'FIXED', '생성 성공', 'MBR 데이터가 생성되었습니다.', 0, NULL, 'MBR created message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010002', 'ko', 'FIXED', '수정 성공', 'MBR 데이터가 수정되었습니다.', 0, NULL, 'MBR updated message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010003', 'ko', 'FIXED', '삭제 성공', 'MBR 데이터가 삭제되었습니다.', 0, NULL, 'MBR deleted message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010101', 'ko', 'FIXED', '잘못된 요청입니다.', 'MBR 요청 형식이 올바르지 않습니다.', 0, NULL, 'MBR bad request message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010102', 'ko', 'INDEXED', '유효하지 않은 파라미터입니다.', 'MBR 파라미터 검증에 실패했습니다. field={0}', 1, '["memberId"]', 'MBR invalid parameter message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010103', 'ko', 'INDEXED', '요청한 자원을 찾을 수 없습니다.', 'MBR 조회 대상이 없습니다. target={0}', 1, '["member"]', 'MBR not found message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010104', 'ko', 'INDEXED', '중복된 데이터가 있습니다.', 'MBR 중복 데이터가 감지되었습니다. key={0}', 1, '["memberId"]', 'MBR duplicate message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010105', 'ko', 'INDEXED', '입력값 검증에 실패했습니다.', 'MBR 입력값 검증에 실패했습니다. field={0}', 1, '["name"]', 'MBR validation message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010106', 'ko', 'FIXED', '인증이 필요합니다.', 'MBR 인증되지 않은 요청입니다.', 0, NULL, 'MBR unauthorized message', 'SYSTEM', 'SYSTEM'),
    ('MMBR010107', 'ko', 'FIXED', '접근 권한이 없습니다.', 'MBR 접근 권한이 없습니다.', 0, NULL, 'MBR forbidden message', 'SYSTEM', 'SYSTEM'),
    ('MMBR030001', 'ko', 'INDEXED', '외부 서비스 오류가 발생했습니다.', 'MBR 외부 서비스 오류가 발생했습니다. service={0}', 1, '["external"]', 'MBR external service message', 'SYSTEM', 'SYSTEM'),
    ('MMBR990000', 'ko', 'INDEXED', '내부 서버 오류가 발생했습니다.', 'MBR 내부 서버 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'MBR internal server message', 'SYSTEM', 'SYSTEM'),
    ('MMBR990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', 'MBR 데이터베이스 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'MBR database message', 'SYSTEM', 'SYSTEM'),
    ('MXYZ090001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=XYZ_EDU_SAMPLE', 2, '["회원번호","M0001"]', 'XYZ dynamic duplicate education message', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF 샘플 시스템에 오신 것을 환영합니다.', 'CMN welcome sample message.', 0, NULL, 'Sample welcome message', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF sample system.', 'CMN welcome sample message.', 0, NULL, 'Sample welcome message', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_format_type = VALUES(message_format_type),
    external_message = VALUES(external_message),
    internal_message = VALUES(internal_message),
    parameter_count = VALUES(parameter_count),
    parameter_sample = VALUES(parameter_sample),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO response_code_table (
    response_code, message_code, result_type, module_id, response_group, sequence_no,
    http_status, description, created_by, updated_by
) VALUES
    ('SPFW000000', 'MPFW000000', 'S', 'PFW', '00', '0000', 200, 'PFW common success', 'SYSTEM', 'SYSTEM'),
    ('EPFW010001', 'MPFW010001', 'E', 'PFW', '01', '0001', 400, 'Invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EPFW010002', 'MPFW010002', 'E', 'PFW', '01', '0002', 404, 'Not found', 'SYSTEM', 'SYSTEM'),
    ('EPFW010003', 'MPFW010003', 'E', 'PFW', '01', '0003', 409, 'Duplicate', 'SYSTEM', 'SYSTEM'),
    ('EPFW010004', 'MPFW010004', 'E', 'PFW', '01', '0004', 400, 'Validation failed', 'SYSTEM', 'SYSTEM'),
    ('EPFW010005', 'MPFW010005', 'E', 'PFW', '01', '0005', 401, 'Unauthorized', 'SYSTEM', 'SYSTEM'),
    ('EPFW010006', 'MPFW010006', 'E', 'PFW', '01', '0006', 403, 'Forbidden', 'SYSTEM', 'SYSTEM'),
    ('EPFW020001', 'MPFW020001', 'E', 'PFW', '02', '0001', 400, 'Business rule violation', 'SYSTEM', 'SYSTEM'),
    ('EPFW030001', 'MPFW030001', 'E', 'PFW', '03', '0001', 502, 'External service error', 'SYSTEM', 'SYSTEM'),
    ('EPFW900001', 'MPFW900001', 'E', 'PFW', '90', '0001', 400, 'Missing transaction header', 'SYSTEM', 'SYSTEM'),
    ('EPFW900002', 'MPFW900002', 'E', 'PFW', '90', '0002', 500, 'Invalid transaction metadata', 'SYSTEM', 'SYSTEM'),
    ('EPFW900003', 'MPFW900003', 'E', 'PFW', '90', '0003', 500, 'Service endpoint not found', 'SYSTEM', 'SYSTEM'),
    ('EPFW900004', 'MPFW900004', 'E', 'PFW', '90', '0004', 400, 'Dynamic log rule invalid', 'SYSTEM', 'SYSTEM'),
    ('EPFW990000', 'MPFW990000', 'E', 'PFW', '99', '0000', 500, 'Internal server error', 'SYSTEM', 'SYSTEM'),
    ('EPFW990001', 'MPFW990001', 'E', 'PFW', '99', '0001', 500, 'Database error', 'SYSTEM', 'SYSTEM'),
    ('SACC000000', 'MACC000000', 'S', 'ACC', '00', '0000', 200, 'ACC common success', 'SYSTEM', 'SYSTEM'),
    ('SACC010000', 'MACC010000', 'S', 'ACC', '01', '0000', 200, 'ACC business success', 'SYSTEM', 'SYSTEM'),
    ('EACC010001', 'MACC010001', 'E', 'ACC', '01', '0001', 400, 'ACC invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EACC010002', 'MACC010002', 'E', 'ACC', '01', '0002', 404, 'ACC not found', 'SYSTEM', 'SYSTEM'),
    ('SMBR000000', 'MMBR000000', 'S', 'MBR', '00', '0000', 200, 'MBR common success', 'SYSTEM', 'SYSTEM'),
    ('SMBR010001', 'MMBR010001', 'S', 'MBR', '01', '0001', 200, 'MBR created', 'SYSTEM', 'SYSTEM'),
    ('SMBR010002', 'MMBR010002', 'S', 'MBR', '01', '0002', 200, 'MBR updated', 'SYSTEM', 'SYSTEM'),
    ('SMBR010003', 'MMBR010003', 'S', 'MBR', '01', '0003', 200, 'MBR deleted', 'SYSTEM', 'SYSTEM'),
    ('EMBR010001', 'MMBR010101', 'E', 'MBR', '01', '0001', 400, 'MBR bad request', 'SYSTEM', 'SYSTEM'),
    ('EMBR010002', 'MMBR010102', 'E', 'MBR', '01', '0002', 400, 'MBR invalid parameter', 'SYSTEM', 'SYSTEM'),
    ('EMBR010003', 'MMBR010103', 'E', 'MBR', '01', '0003', 404, 'MBR not found', 'SYSTEM', 'SYSTEM'),
    ('EMBR010004', 'MMBR010104', 'E', 'MBR', '01', '0004', 409, 'MBR duplicate', 'SYSTEM', 'SYSTEM'),
    ('EMBR010005', 'MMBR010105', 'E', 'MBR', '01', '0005', 400, 'MBR validation failed', 'SYSTEM', 'SYSTEM'),
    ('EMBR010006', 'MMBR010106', 'E', 'MBR', '01', '0006', 401, 'MBR unauthorized', 'SYSTEM', 'SYSTEM'),
    ('EMBR010007', 'MMBR010107', 'E', 'MBR', '01', '0007', 403, 'MBR forbidden', 'SYSTEM', 'SYSTEM'),
    ('EMBR030001', 'MMBR030001', 'E', 'MBR', '03', '0001', 502, 'MBR external service error', 'SYSTEM', 'SYSTEM'),
    ('EMBR990000', 'MMBR990000', 'E', 'MBR', '99', '0000', 500, 'MBR internal server error', 'SYSTEM', 'SYSTEM'),
    ('EMBR990001', 'MMBR990001', 'E', 'MBR', '99', '0001', 500, 'MBR database error', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_code = VALUES(message_code),
    result_type = VALUES(result_type),
    module_id = VALUES(module_id),
    response_group = VALUES(response_group),
    sequence_no = VALUES(sequence_no),
    http_status = VALUES(http_status),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO config_table (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES
    ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'Preload CMN cache at startup', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', 'Fail startup when CMN cache preload fails', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', 'Cache refresh event polling interval in milliseconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.PERIODIC_REFRESH_MILLIS', '1800000', 'NUMBER', 'Periodic cache refresh interval in milliseconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', 'Default CMN message broker type', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.DEFAULT_DESTINATION', 'cpf.default.event', 'STRING', 'Default messaging destination', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.SSH_ENABLED', 'N', 'BOOLEAN', 'Allow SSH/SCP/SFTP execution', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.DRY_RUN', 'Y', 'BOOLEAN', 'Plan remote file operations without execution', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.FILE.TIMEOUT_SECONDS', '15', 'NUMBER', 'File exchange timeout seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.ISSUER', 'CPF', 'STRING', 'Sample JWT issuer', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.AUDIENCE', 'CPF-API', 'STRING', 'Sample JWT audience', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.SECURITY.JWT.TTL_SECONDS', '300', 'NUMBER', 'Sample JWT TTL seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'PFW HTTP client connect timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'PFW HTTP client read timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.SESSION_TTL_SECONDS', '3600', 'NUMBER', 'ADM session TTL seconds', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'ADM password expiration days', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MIN_LENGTH', '10', 'NUMBER', 'ADM password minimum length', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MAX_FAIL_COUNT', '5', 'NUMBER', 'ADM login failure lock threshold', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.FEATURE.SAMPLE_ENABLED', 'Y', 'BOOLEAN', 'Enable sample APIs and education flows', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    config_type = VALUES(config_type),
    description = VALUES(description),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO security_jwt_key (
    KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, CREATED_BY, UPDATED_BY
) VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    ISSUER = VALUES(ISSUER),
    ALGORITHM = VALUES(ALGORITHM),
    SECRET_REF = VALUES(SECRET_REF),
    ACTIVE_YN = VALUES(ACTIVE_YN),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO cache_refresh_event (
    cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by
)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

-- END specs/sql/50_framework_seed_data.sql

-- ============================================================================
-- BEGIN specs/sql/60_adm_seed_data.sql
-- ============================================================================
-- ADM initial roles, menus, permissions, and local/test account.
-- Target DB: admDB

USE admDB;

INSERT INTO operator_role (ROLE_ID, ROLE_NAME, DESCRIPTION, USE_YN, CREATED_BY, UPDATED_BY)
VALUES
    ('ADM_ADMIN', 'Framework Administrator', 'Can manage every ADM menu and operation.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', 'Operations User', 'Can query logs, refresh caches, and manage dynamic log levels.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', 'Read Only User', 'Can query logs and settings without changing data.', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    ROLE_NAME = VALUES(ROLE_NAME),
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_menu (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, CREATED_BY, UPDATED_BY)
VALUES
    ('DASHBOARD', NULL, 'Dashboard', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, 'Transaction Logs', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, 'Cache Management', '/adm#cache', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, 'Response Codes', '/adm#response-codes', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, 'Dynamic Log Level', '/adm#log-level', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, 'Audit Logs', '/adm#audit-logs', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, 'Operator Management', '/adm#operators', 70, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    PARENT_MENU_ID = VALUES(PARENT_MENU_ID),
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_user (
    OPERATOR_ID,
    OPERATOR_NAME,
    PASSWORD_HASH,
    LOCKED_YN,
    FAIL_COUNT,
    PASSWORD_CHANGED_AT,
    PASSWORD_CHANGE_REQUIRED_YN,
    USE_YN,
    CREATED_BY,
    UPDATED_BY
) VALUES (
    'admin',
    'Local Administrator',
    'PBKDF2$120000$AQIDBAUGBwgJCgsMDQ4PEA==$cjgjgGQwgcZ0+fFaA8Z4qBJkZfszRZ73BSBIMXAJkqI=',
    'N',
    0,
    DATE_SUB(NOW(), INTERVAL 91 DAY),
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    OPERATOR_NAME = VALUES(OPERATOR_NAME),
    PASSWORD_HASH = VALUES(PASSWORD_HASH),
    LOCKED_YN = VALUES(LOCKED_YN),
    FAIL_COUNT = VALUES(FAIL_COUNT),
    PASSWORD_CHANGE_REQUIRED_YN = VALUES(PASSWORD_CHANGE_REQUIRED_YN),
    USE_YN = VALUES(USE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_user_role (OPERATOR_ID, ROLE_ID, CREATED_BY, UPDATED_BY)
VALUES ('admin', 'ADM_ADMIN', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_BY, UPDATED_BY)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM operator_menu
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_BY, UPDATED_BY)
SELECT 'ADM_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('CACHE', 'DYNAMIC_LOG') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID = 'DYNAMIC_LOG' THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM operator_menu
WHERE MENU_ID NOT IN ('OPERATOR', 'RESPONSE_CODE')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_BY, UPDATED_BY)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM operator_menu
WHERE MENU_ID NOT IN ('DYNAMIC_LOG', 'OPERATOR', 'RESPONSE_CODE', 'AUDIT_LOG')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

INSERT INTO operator_audit_log (
    TRANSACTION_ID,
    TRACE_ID,
    OPERATOR_ID,
    MENU_ID,
    ACTION_TYPE,
    TARGET_TYPE,
    TARGET_ID,
    REASON,
    REQUEST_BODY,
    CLIENT_IP,
    CREATED_BY,
    UPDATED_BY
) VALUES (
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'admin',
    'DASHBOARD',
    'SEED',
    'ADM',
    'INITIAL_DATA',
    'Initial ADM seed data registered.',
    NULL,
    '127.0.0.1',
    'SYSTEM',
    'SYSTEM'
);

-- END specs/sql/60_adm_seed_data.sql

-- ============================================================================
-- BEGIN specs/sql/70_test_data.sql
-- ============================================================================
-- Local and integration test data for ACC, MBR, PFW, and ADM screens.

USE pfwDB;

INSERT INTO file_exchange_log (
    EXCHANGE_ID,
    TRANSACTION_ID,
    TRACE_ID,
    BUSINESS_TRANSACTION_ID,
    ACTION_TYPE,
    PROTOCOL,
    DIRECTION,
    EXECUTED_YN,
    SUCCESS_YN,
    HOST,
    SOURCE_PATH,
    TARGET_PATH,
    REQUEST_USER,
    MESSAGE,
    CREATED_BY,
    UPDATED_BY
) VALUES (
    'FILE-LOCAL-SAMPLE-001',
    'TEST_TRANSACTION',
    'TEST_TRACE',
    'XYZ08EDU0001',
    'LOCAL_WRITE',
    'LOCAL',
    'WRITE',
    'Y',
    'Y',
    'localhost',
    '/tmp/fps/source.txt',
    '/tmp/fps/target.txt',
    'SYSTEM',
    'Local file exchange sample history.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MESSAGE = VALUES(MESSAGE),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

USE accDB;

INSERT INTO acc_account (account_id, account_no, account_name, account_status, balance, description, created_by, updated_by)
VALUES
    (1, '100-000-000001', 'ACC sample account 1', 'ACTIVE', 100000.00, 'ACC account sample 1', 'SYSTEM', 'SYSTEM'),
    (2, '100-000-000002', 'ACC sample account 2', 'ACTIVE', 250000.00, 'ACC account sample 2', 'SYSTEM', 'SYSTEM'),
    (3, '100-000-000003', 'ACC dormant account', 'DORMANT', 0.00, 'ACC dormant account sample', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    account_no = VALUES(account_no),
    account_name = VALUES(account_name),
    account_status = VALUES(account_status),
    balance = VALUES(balance),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE mbrDB;

INSERT INTO member (id, name, description, created_by, updated_by)
VALUES
    (1, 'mbr 1', 'MBR separated DB sample member 1', 'SYSTEM', 'SYSTEM'),
    (2, 'mbr 2', 'MBR separated DB sample member 2', 'SYSTEM', 'SYSTEM'),
    (3, 'mbr 3', 'MBR separated DB sample member 3', 'SYSTEM', 'SYSTEM'),
    (100, 'search target', 'MBR separated DB name search test row', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE pfwDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';

INSERT INTO TRAN_LOG (
    LOG_DATE,
    TRANSACTION_ID,
    TRACE_ID,
    SPAN_ID,
    SEQUENCE_NO,
    MODULE_ID,
    BUSINESS_TRANSACTION_ID,
    BUSINESS_TRANSACTION_NAME,
    LOG_TYPE,
    REQUEST_TYPE,
    ORIGINAL_CHANNEL_CODE,
    CHANNEL_CODE,
    MEMBER_NO,
    CUSTOMER_NO,
    SCREEN_ID,
    DEVICE_ID,
    WAS_ID,
    HTTP_METHOD,
    URI,
    CONTROLLER,
    EXECUTION_PACKAGE,
    EXECUTION_CLASS,
    EXECUTION_METHOD,
    EXECUTION_SIGNATURE,
    PARAMETERS,
    RESPONSE,
    RESPONSE_CODE,
    EXEC_USER,
    CLIENT_IP,
    USER_AGENT,
    START_TIME,
    END_TIME,
    DURATION_MS,
    CREATED_BY,
    UPDATED_BY
)
SELECT
    CURDATE(),
    @sample_transaction_id,
    'trace-sample-001',
    'span-sample-001',
    1,
    'MBR',
    'MBR01BSE0001',
    'MBR member list sample',
    'SUCCESS',
    'NORMAL',
    'WEB',
    'WEB',
    'M000000001',
    'C000000001',
    'MBR_LIST',
    'LOCAL_BROWSER',
    'local01',
    'GET',
    '/mbr/list',
    'fps.mbr.bse.controller.MbrController',
    'fps.mbr.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"code":"1000","message":"success"}',
    200,
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    NOW(3),
    NOW(3),
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM TRAN_LOG
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM TRAN_LOG
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO TRAN_LOG_DTL (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    CREATED_BY,
    UPDATED_BY
)
SELECT @sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL"}', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM TRAN_LOG_DTL
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO TRAN_LOG_DTL (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    CREATED_BY,
    UPDATED_BY
)
SELECT @sample_log_idx, 'memo', 'Seed transaction log for ADM log screen smoke test.', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM TRAN_LOG_DTL
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

USE admDB;

INSERT INTO dynamic_log_level_rule (
    RULE_ID,
    TRANSACTION_ID,
    BUSINESS_TRANSACTION_ID,
    MODULE_ID,
    LOG_LEVEL,
    EXPIRE_AT,
    REASON,
    USE_YN,
    CREATED_BY,
    UPDATED_BY
) VALUES (
    'sample-rule-001',
    NULL,
    'MBR01BSE0001',
    'MBR',
    'DEBUG',
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'Initial sample dynamic log rule for ADM screen smoke test.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    BUSINESS_TRANSACTION_ID = VALUES(BUSINESS_TRANSACTION_ID),
    MODULE_ID = VALUES(MODULE_ID),
    LOG_LEVEL = VALUES(LOG_LEVEL),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    REASON = VALUES(REASON),
    USE_YN = VALUES(USE_YN),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

-- END specs/sql/70_test_data.sql

-- ============================================================================
-- BEGIN specs/sql/99_smoke_check.sql
-- ============================================================================
-- CPF initial database smoke check.
-- Run after 01_create_databases.sql through 70_test_data.sql.

SELECT 'pfwDB.TRAN_LOG' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG;
SELECT 'pfwDB.TRAN_LOG_DTL' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG_DTL;

SELECT 'pfwDB.code_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.code_table;
SELECT 'pfwDB.message_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.message_table;
SELECT 'pfwDB.response_code_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.response_code_table;
SELECT 'pfwDB.config_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.config_table;
SELECT 'pfwDB.cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.cache_refresh_event;

SELECT 'admDB.operator_user' AS check_name, COUNT(*) AS row_count FROM admDB.operator_user;
SELECT 'admDB.operator_menu' AS check_name, COUNT(*) AS row_count FROM admDB.operator_menu;
SELECT 'admDB.operator_role' AS check_name, COUNT(*) AS row_count FROM admDB.operator_role;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'mbrDB.member' AS check_name, COUNT(*) AS row_count FROM mbrDB.member;

SELECT response_code, message_code, result_type, http_status
FROM pfwDB.response_code_table
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM pfwDB.message_table
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;

-- END specs/sql/99_smoke_check.sql
