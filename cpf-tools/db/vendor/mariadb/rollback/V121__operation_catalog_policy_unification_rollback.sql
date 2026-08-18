
-- Fail closed if post-V121 identifiers cannot fit the legacy schema.
DROP PROCEDURE IF EXISTS CPF_V121_ROLLBACK_GUARD;
DELIMITER $$
CREATE PROCEDURE CPF_V121_ROLLBACK_GUARD()
BEGIN
  IF EXISTS (SELECT 1 FROM OPS_OPERATION_CATALOG WHERE CHAR_LENGTH(operation_id) > 20 OR CHAR_LENGTH(system_code) > 20) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='CPF V121 rollback blocked: current operation/system identifiers exceed legacy widths';
  END IF;
END$$
DELIMITER ;
CALL CPF_V121_ROLLBACK_GUARD();
DROP PROCEDURE CPF_V121_ROLLBACK_GUARD;

CREATE TABLE CPF_TRANSACTION_META (
    transaction_id VARCHAR(20) NOT NULL,
    transaction_name VARCHAR(150) NOT NULL,
    module_code VARCHAR(20) NOT NULL,
    domain_code VARCHAR(50) NULL,
    http_method VARCHAR(20) DEFAULT 'ANY' NOT NULL,
    api_path VARCHAR(500) NOT NULL,
    controller_class VARCHAR(255) NOT NULL,
    handler_method VARCHAR(150) NOT NULL,
    swagger_operation_id VARCHAR(150) NULL,
    log_policy_key VARCHAR(120) NULL,
    sensitive_yn CHAR(1) DEFAULT 'N' NOT NULL,
    masking_policy_key VARCHAR(120) NULL,
    active_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    first_detected_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_detected_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_scanned_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_CPF_TRANSACTION_META PRIMARY KEY (transaction_id)
) ENGINE=InnoDB;
ALTER TABLE CPF_TRANSACTION_META COMMENT = 'CPF 온라인 거래 메타';
CREATE INDEX ix_cpf_transaction_meta_module ON CPF_TRANSACTION_META (module_code, domain_code, active_yn);
CREATE INDEX ix_cpf_transaction_meta_path ON CPF_TRANSACTION_META (http_method, api_path);
CREATE INDEX ix_cpf_transaction_meta_policy ON CPF_TRANSACTION_META (log_policy_key, active_yn);
CREATE INDEX ix_cpf_transaction_meta_scan ON CPF_TRANSACTION_META (active_yn, last_scanned_at);

INSERT INTO CPF_TRANSACTION_META
(transaction_id, transaction_name, module_code, domain_code, http_method, api_path, controller_class, handler_method,
 swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key, active_yn, first_detected_at, last_detected_at,
 last_scanned_at, created_by, created_at, updated_by, updated_at)
SELECT c.operation_id, c.operation_name, c.system_code, c.domain_code, c.http_method, c.api_path, c.controller_class, c.handler_method,
       c.openapi_operation_id, c.log_policy_key, c.sensitive_yn, c.masking_policy_key, COALESCE(p.enabled_yn,'N'),
       c.first_seen_at, c.last_seen_at, c.last_seen_at, c.created_by, c.created_at, c.updated_by, c.updated_at
FROM OPS_OPERATION_CATALOG c LEFT JOIN OPS_OPERATION_POLICY p ON p.operation_id=c.operation_id;

DROP TABLE OPS_OPERATION_CALLER_POLICY;
DROP TABLE OPS_OPERATION_POLICY;
DROP TABLE OPS_SYSTEM_DOMAIN_ACCESS;
DROP TABLE OPS_OPERATION_DISCOVERY_INSTANCE;
DROP TABLE OPS_OPERATION_CATALOG;
DROP TABLE OPS_SYSTEM_REGISTRY;
