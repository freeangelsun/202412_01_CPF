-- CPF V121: unify business Operation catalog and ADM-owned runtime policy.
-- Historical V63 and earlier migrations remain immutable.

CREATE TABLE OPS_SYSTEM_REGISTRY (
    system_code VARCHAR(32) NOT NULL,
    system_name VARCHAR(120) NOT NULL,
    domain_code VARCHAR(50) NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    description VARCHAR(500) NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    first_seen_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_seen_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_instance_id VARCHAR(160) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_SYSTEM_REGISTRY PRIMARY KEY (system_code),
    CONSTRAINT ck_ops_system_registry_enabled CHECK (enabled_yn IN ('Y', 'N'))
);
COMMENT ON TABLE OPS_SYSTEM_REGISTRY IS 'CPF 업무 System Registry';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.system_code IS 'CPF System 코드';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.system_name IS 'System 명';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.domain_code IS '소유 Domain 코드';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.enabled_yn IS '등록 System 활성 여부';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.description IS 'System 설명';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.policy_version IS 'System 정책 버전';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.first_seen_at IS '최초 Runtime 발견 시각';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.last_seen_at IS '최근 Runtime 발견 시각';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.last_instance_id IS '최근 발견 Runtime instanceId';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.created_by IS '등록자';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.created_at IS '등록일시';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_SYSTEM_REGISTRY.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_system_registry_domain ON OPS_SYSTEM_REGISTRY (domain_code, enabled_yn);
CREATE INDEX ix_ops_system_registry_seen ON OPS_SYSTEM_REGISTRY (last_seen_at, enabled_yn);

CREATE TABLE OPS_OPERATION_CATALOG (
    operation_id VARCHAR(160) NOT NULL,
    operation_name VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NULL,
    system_code VARCHAR(32) NOT NULL,
    domain_code VARCHAR(50) NULL,
    application_code VARCHAR(100) NULL,
    http_method VARCHAR(20) DEFAULT 'ANY' NOT NULL,
    api_path VARCHAR(500) NOT NULL,
    controller_class VARCHAR(255) NOT NULL,
    handler_method VARCHAR(150) NOT NULL,
    openapi_operation_id VARCHAR(160) NULL,
    source_fingerprint VARCHAR(64) NULL,
    discovery_status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
    first_seen_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_seen_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_instance_id VARCHAR(160) NULL,
    metadata_version BIGINT DEFAULT 1 NOT NULL,
    log_policy_key VARCHAR(120) NULL,
    sensitive_yn CHAR(1) DEFAULT 'N' NOT NULL,
    masking_policy_key VARCHAR(120) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_OPERATION_CATALOG PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_catalog_discovery CHECK (discovery_status IN ('ACTIVE', 'NOT_DISCOVERED')),
    CONSTRAINT ck_ops_operation_catalog_sensitive CHECK (sensitive_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_catalog_system FOREIGN KEY (system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code)
);
COMMENT ON TABLE OPS_OPERATION_CATALOG IS 'CPF 업무 Online Operation Canonical Catalog';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.operation_id IS '업무 Domain Canonical operationId';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.operation_name IS '업무 Operation 명';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.description IS '업무 Operation 설명';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.system_code IS 'Operation 소유 System 코드';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.domain_code IS 'Operation 소유 Domain 코드';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.application_code IS '발견 Application 코드';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.http_method IS 'HTTP 메서드';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.api_path IS 'Canonical API 경로';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.controller_class IS 'Controller 클래스명';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.handler_method IS 'Handler 메서드명';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.openapi_operation_id IS '업무 API OpenAPI operationId';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.source_fingerprint IS 'Source/Handler 계약 SHA-256 fingerprint';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.discovery_status IS 'ACTIVE 또는 NOT_DISCOVERED 발견상태';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.first_seen_at IS '최초 발견 시각';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.last_seen_at IS '최근 발견 시각';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.last_instance_id IS '최근 발견 Runtime instanceId';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.metadata_version IS 'Source metadata optimistic version';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.log_policy_key IS '연결 로그 정책 키';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.sensitive_yn IS '민감 Operation 여부';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.masking_policy_key IS '마스킹 정책 키';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.created_by IS '등록자';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.created_at IS '등록일시';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_OPERATION_CATALOG.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_operation_catalog_system ON OPS_OPERATION_CATALOG (system_code, domain_code, discovery_status);
CREATE INDEX ix_ops_operation_catalog_path ON OPS_OPERATION_CATALOG (http_method, api_path);
CREATE INDEX ix_ops_operation_catalog_seen ON OPS_OPERATION_CATALOG (last_seen_at, discovery_status);

CREATE TABLE OPS_OPERATION_DISCOVERY_INSTANCE (
    operation_id VARCHAR(160) NOT NULL,
    instance_id VARCHAR(160) NOT NULL,
    system_code VARCHAR(32) NOT NULL,
    application_code VARCHAR(100) NULL,
    artifact_version VARCHAR(120) NULL,
    artifact_commit VARCHAR(120) NULL,
    discovered_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    last_reported_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_OPERATION_DISCOVERY_INSTANCE PRIMARY KEY (operation_id, instance_id),
    CONSTRAINT ck_ops_operation_discovery_yn CHECK (discovered_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_discovery_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
);
COMMENT ON TABLE OPS_OPERATION_DISCOVERY_INSTANCE IS 'CPF 업무 Operation Runtime/Artifact별 Source discovery evidence';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.operation_id IS '업무 Domain Canonical operationId';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.instance_id IS 'Operation을 스캔한 Runtime instanceId';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.system_code IS '스캔 Runtime System 코드';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.application_code IS '스캔 Application 코드';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.artifact_version IS '스캔 Artifact version';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.artifact_commit IS '스캔 Artifact commit/SHA';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.discovered_yn IS '해당 Runtime/Artifact에서 발견 여부';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.last_reported_at IS '최근 discovery report 시각';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.created_by IS '등록자';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.created_at IS '등록일시';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_OPERATION_DISCOVERY_INSTANCE.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_operation_discovery_instance ON OPS_OPERATION_DISCOVERY_INSTANCE (instance_id, discovered_yn, last_reported_at);
CREATE INDEX ix_ops_operation_discovery_scope ON OPS_OPERATION_DISCOVERY_INSTANCE (system_code, application_code, discovered_yn);


CREATE TABLE OPS_SYSTEM_DOMAIN_ACCESS (
    caller_system_code VARCHAR(32) NOT NULL,
    target_system_code VARCHAR(32) NOT NULL,
    allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_SYSTEM_DOMAIN_ACCESS PRIMARY KEY (caller_system_code, target_system_code),
    CONSTRAINT ck_ops_system_domain_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_sys_domain_caller FOREIGN KEY (caller_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE,
    CONSTRAINT fk_ops_sys_domain_target FOREIGN KEY (target_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE
);
COMMENT ON TABLE OPS_SYSTEM_DOMAIN_ACCESS IS 'CPF System/Domain 1차 호출 정책';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.caller_system_code IS '호출 System 코드';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.target_system_code IS '대상 System 코드';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.allowed_yn IS 'System/Domain 1차 호출 허용 여부';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.policy_version IS '정책 버전';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.change_reason IS '최근 변경 사유';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.created_by IS '등록자';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.created_at IS '등록일시';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_SYSTEM_DOMAIN_ACCESS.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_system_domain_access_target ON OPS_SYSTEM_DOMAIN_ACCESS (target_system_code, allowed_yn);

CREATE TABLE OPS_OPERATION_POLICY (
    operation_id VARCHAR(160) NOT NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    all_callers_yn CHAR(1) DEFAULT 'N' NOT NULL,
    channel_policy_required_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    seed_source VARCHAR(80) NULL,
    seed_revision VARCHAR(120) NULL,
    seeded_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_OPERATION_POLICY PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_policy_enabled CHECK (enabled_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_all CHECK (all_callers_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_channel CHECK (channel_policy_required_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_policy_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
);
COMMENT ON TABLE OPS_OPERATION_POLICY IS 'CPF 업무 Operation ADM-owned 실행 정책';
COMMENT ON COLUMN OPS_OPERATION_POLICY.operation_id IS '정책 대상 operationId';
COMMENT ON COLUMN OPS_OPERATION_POLICY.enabled_yn IS 'Operation 실행 허용 여부';
COMMENT ON COLUMN OPS_OPERATION_POLICY.all_callers_yn IS '등록·활성 Caller 전체 허용 여부';
COMMENT ON COLUMN OPS_OPERATION_POLICY.channel_policy_required_yn IS 'Channel 3차 정책 적용 필요 여부';
COMMENT ON COLUMN OPS_OPERATION_POLICY.policy_version IS 'Operation 정책 버전';
COMMENT ON COLUMN OPS_OPERATION_POLICY.seed_source IS '최초 Seed 출처';
COMMENT ON COLUMN OPS_OPERATION_POLICY.seed_revision IS '최초 Seed revision';
COMMENT ON COLUMN OPS_OPERATION_POLICY.seeded_at IS '최초 Seed 적용 시각';
COMMENT ON COLUMN OPS_OPERATION_POLICY.change_reason IS '최근 변경 사유';
COMMENT ON COLUMN OPS_OPERATION_POLICY.created_by IS '등록자';
COMMENT ON COLUMN OPS_OPERATION_POLICY.created_at IS '등록일시';
COMMENT ON COLUMN OPS_OPERATION_POLICY.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_OPERATION_POLICY.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_operation_policy_enabled ON OPS_OPERATION_POLICY (enabled_yn, policy_version);

CREATE TABLE OPS_OPERATION_CALLER_POLICY (
    operation_id VARCHAR(160) NOT NULL,
    caller_system_code VARCHAR(32) NOT NULL,
    allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    seed_source VARCHAR(80) NULL,
    seed_revision VARCHAR(120) NULL,
    seeded_at TIMESTAMP(3) WITHOUT TIME ZONE NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_OPS_OPERATION_CALLER_POLICY PRIMARY KEY (operation_id, caller_system_code),
    CONSTRAINT ck_ops_operation_caller_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_caller_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
);
COMMENT ON TABLE OPS_OPERATION_CALLER_POLICY IS 'CPF 업무 Operation Caller별 ADM-owned 정책';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.operation_id IS '정책 대상 operationId';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.caller_system_code IS '허용/거부 Caller System 코드';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.allowed_yn IS 'Caller별 Operation 호출 허용 여부';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.policy_version IS 'Caller 정책 버전';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.seed_source IS '최초 Seed 출처';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.seed_revision IS '최초 Seed revision';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.seeded_at IS '최초 Seed 적용 시각';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.change_reason IS '최근 변경 사유';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.created_by IS '등록자';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.created_at IS '등록일시';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.updated_by IS '최종 수정자';
COMMENT ON COLUMN OPS_OPERATION_CALLER_POLICY.updated_at IS '최종 수정일시';
CREATE INDEX ix_ops_operation_caller_caller ON OPS_OPERATION_CALLER_POLICY (caller_system_code, allowed_yn);


CREATE OR REPLACE FUNCTION FN_OPS_OPERATION_TOUCH() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER TRG_OPS_SYSTEM_REGISTRY_TOUCH BEFORE UPDATE ON OPS_SYSTEM_REGISTRY FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();
CREATE TRIGGER TRG_OPS_OPERATION_CATALOG_TOUCH BEFORE UPDATE ON OPS_OPERATION_CATALOG FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();
CREATE TRIGGER TRG_OPS_OPERATION_DISCOVERY_INSTANCE_TOUCH BEFORE UPDATE ON OPS_OPERATION_DISCOVERY_INSTANCE FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();
CREATE TRIGGER TRG_OPS_SYSTEM_DOMAIN_ACCESS_TOUCH BEFORE UPDATE ON OPS_SYSTEM_DOMAIN_ACCESS FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();
CREATE TRIGGER TRG_OPS_OPERATION_POLICY_TOUCH BEFORE UPDATE ON OPS_OPERATION_POLICY FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();
CREATE TRIGGER TRG_OPS_OPERATION_CALLER_POLICY_TOUCH BEFORE UPDATE ON OPS_OPERATION_CALLER_POLICY FOR EACH ROW EXECUTE FUNCTION FN_OPS_OPERATION_TOUCH();


-- Preserve the legacy transaction-metadata rows as source metadata only. Runtime re-scan
-- confirms ACTIVE discovery state and never overwrites the ADM-owned policy after this migration.
INSERT INTO OPS_SYSTEM_REGISTRY
(system_code, system_name, domain_code, enabled_yn, description, policy_version, first_seen_at, last_seen_at, last_instance_id, created_by, created_at, updated_by, updated_at)
SELECT UPPER(module_code), MAX(module_code), MAX(domain_code), 'Y', 'Migrated from CPF_TRANSACTION_META', 1,
       MIN(first_detected_at), MAX(last_detected_at), NULL, 'CPF_MIGRATION', MIN(created_at), 'CPF_MIGRATION', MAX(updated_at)
FROM CPF_TRANSACTION_META
GROUP BY UPPER(module_code);

INSERT INTO OPS_OPERATION_CATALOG
(operation_id, operation_name, description, system_code, domain_code, application_code, http_method, api_path,
 controller_class, handler_method, openapi_operation_id, source_fingerprint, discovery_status, first_seen_at,
 last_seen_at, last_instance_id, metadata_version, log_policy_key, sensitive_yn, masking_policy_key,
 created_by, created_at, updated_by, updated_at)
SELECT transaction_id, transaction_name, NULL, UPPER(module_code), domain_code, NULL, http_method, api_path,
       controller_class, handler_method, swagger_operation_id, NULL, 'NOT_DISCOVERED', first_detected_at,
       last_detected_at, NULL, 1, log_policy_key, sensitive_yn, masking_policy_key,
       created_by, created_at, updated_by, updated_at
FROM CPF_TRANSACTION_META;

INSERT INTO OPS_OPERATION_POLICY
(operation_id, enabled_yn, all_callers_yn, channel_policy_required_yn, policy_version, seed_source, seed_revision,
 seeded_at, change_reason, created_by, created_at, updated_by, updated_at)
SELECT transaction_id, active_yn, 'N', 'N', 1, 'MIGRATION', 'V121', CURRENT_TIMESTAMP(3),
       'Migrated from legacy transaction metadata; caller policy remains fail-closed until explicitly configured',
       created_by, created_at, updated_by, updated_at
FROM CPF_TRANSACTION_META;

DROP TABLE CPF_TRANSACTION_META;
