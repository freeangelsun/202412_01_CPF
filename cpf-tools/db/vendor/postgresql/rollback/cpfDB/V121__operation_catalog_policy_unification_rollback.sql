
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM OPS_OPERATION_CATALOG WHERE LENGTH(operation_id) > 20 OR LENGTH(system_code) > 20) THEN
    RAISE EXCEPTION 'CPF V121 rollback blocked: current operation/system identifiers exceed legacy widths';
  END IF;
END $$;

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
    first_detected_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_detected_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_scanned_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_CPF_TRANSACTION_META PRIMARY KEY (transaction_id)
);
COMMENT ON TABLE CPF_TRANSACTION_META IS 'CPF 온라인 거래 메타';
COMMENT ON COLUMN CPF_TRANSACTION_META.transaction_id IS '업무 거래 정의 ID(실행 transactionId와 별개)';
COMMENT ON COLUMN CPF_TRANSACTION_META.transaction_name IS '업무 거래명';
COMMENT ON COLUMN CPF_TRANSACTION_META.module_code IS '모듈 코드';
COMMENT ON COLUMN CPF_TRANSACTION_META.domain_code IS '업무 영역 코드';
COMMENT ON COLUMN CPF_TRANSACTION_META.http_method IS 'HTTP 메서드';
COMMENT ON COLUMN CPF_TRANSACTION_META.api_path IS 'API 경로';
COMMENT ON COLUMN CPF_TRANSACTION_META.controller_class IS 'Controller 클래스명';
COMMENT ON COLUMN CPF_TRANSACTION_META.handler_method IS 'Handler 메서드명';
COMMENT ON COLUMN CPF_TRANSACTION_META.swagger_operation_id IS 'Swagger operation 식별자';
COMMENT ON COLUMN CPF_TRANSACTION_META.log_policy_key IS '연결 로그 정책 키';
COMMENT ON COLUMN CPF_TRANSACTION_META.sensitive_yn IS '민감 거래 여부';
COMMENT ON COLUMN CPF_TRANSACTION_META.masking_policy_key IS '마스킹 정책 키';
COMMENT ON COLUMN CPF_TRANSACTION_META.active_yn IS '활성 여부';
COMMENT ON COLUMN CPF_TRANSACTION_META.first_detected_at IS '최초 감지일시';
COMMENT ON COLUMN CPF_TRANSACTION_META.last_detected_at IS '최근 감지일시';
COMMENT ON COLUMN CPF_TRANSACTION_META.last_scanned_at IS '최근 스캔일시';
COMMENT ON COLUMN CPF_TRANSACTION_META.created_by IS '등록자';
COMMENT ON COLUMN CPF_TRANSACTION_META.created_at IS '등록일시';
COMMENT ON COLUMN CPF_TRANSACTION_META.updated_by IS '수정자';
COMMENT ON COLUMN CPF_TRANSACTION_META.updated_at IS '수정일시';
CREATE INDEX ix_cpf_transaction_meta_module ON CPF_TRANSACTION_META (module_code, domain_code, active_yn);
CREATE INDEX ix_cpf_transaction_meta_path ON CPF_TRANSACTION_META (http_method, api_path);
CREATE INDEX ix_cpf_transaction_meta_policy ON CPF_TRANSACTION_META (log_policy_key, active_yn);
CREATE INDEX ix_cpf_transaction_meta_scan ON CPF_TRANSACTION_META (active_yn, last_scanned_at);

CREATE OR REPLACE FUNCTION fn_cpf_transaction_meta_touch() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_cpf_transaction_meta_touch BEFORE UPDATE ON CPF_TRANSACTION_META
FOR EACH ROW EXECUTE FUNCTION fn_cpf_transaction_meta_touch();

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
DROP FUNCTION IF EXISTS FN_OPS_OPERATION_TOUCH();
