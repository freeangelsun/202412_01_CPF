-- CPF V121: unify business Operation catalog and ADM-owned runtime policy.
-- Historical V63 and earlier migrations remain immutable.

CREATE TABLE OPS_SYSTEM_REGISTRY (
    system_code VARCHAR(32) NOT NULL,
    system_name VARCHAR(120) NOT NULL,
    domain_code VARCHAR(50) NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    description VARCHAR(500) NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    first_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_instance_id VARCHAR(160) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_SYSTEM_REGISTRY PRIMARY KEY (system_code),
    CONSTRAINT ck_ops_system_registry_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB;
ALTER TABLE OPS_SYSTEM_REGISTRY COMMENT = 'CPF 업무 System Registry';
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
    first_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_seen_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    last_instance_id VARCHAR(160) NULL,
    metadata_version BIGINT DEFAULT 1 NOT NULL,
    log_policy_key VARCHAR(120) NULL,
    sensitive_yn CHAR(1) DEFAULT 'N' NOT NULL,
    masking_policy_key VARCHAR(120) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_OPERATION_CATALOG PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_catalog_discovery CHECK (discovery_status IN ('ACTIVE', 'NOT_DISCOVERED')),
    CONSTRAINT ck_ops_operation_catalog_sensitive CHECK (sensitive_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_catalog_system FOREIGN KEY (system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code)
) ENGINE=InnoDB;
ALTER TABLE OPS_OPERATION_CATALOG COMMENT = 'CPF 업무 Online Operation Canonical Catalog';
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
    last_reported_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_OPERATION_DISCOVERY_INSTANCE PRIMARY KEY (operation_id, instance_id),
    CONSTRAINT ck_ops_operation_discovery_yn CHECK (discovered_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_discovery_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
) ENGINE=InnoDB;
ALTER TABLE OPS_OPERATION_DISCOVERY_INSTANCE COMMENT = 'CPF 업무 Operation Runtime/Artifact별 Source discovery evidence';
CREATE INDEX ix_ops_operation_discovery_instance ON OPS_OPERATION_DISCOVERY_INSTANCE (instance_id, discovered_yn, last_reported_at);
CREATE INDEX ix_ops_operation_discovery_scope ON OPS_OPERATION_DISCOVERY_INSTANCE (system_code, application_code, discovered_yn);


CREATE TABLE OPS_SYSTEM_DOMAIN_ACCESS (
    caller_system_code VARCHAR(32) NOT NULL,
    target_system_code VARCHAR(32) NOT NULL,
    allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_SYSTEM_DOMAIN_ACCESS PRIMARY KEY (caller_system_code, target_system_code),
    CONSTRAINT ck_ops_system_domain_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_sys_domain_caller FOREIGN KEY (caller_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE,
    CONSTRAINT fk_ops_sys_domain_target FOREIGN KEY (target_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE
) ENGINE=InnoDB;
ALTER TABLE OPS_SYSTEM_DOMAIN_ACCESS COMMENT = 'CPF System/Domain 1차 호출 정책';
CREATE INDEX ix_ops_system_domain_access_target ON OPS_SYSTEM_DOMAIN_ACCESS (target_system_code, allowed_yn);

CREATE TABLE OPS_OPERATION_POLICY (
    operation_id VARCHAR(160) NOT NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    all_callers_yn CHAR(1) DEFAULT 'N' NOT NULL,
    channel_policy_required_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    seed_source VARCHAR(80) NULL,
    seed_revision VARCHAR(120) NULL,
    seeded_at DATETIME(3) NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_OPERATION_POLICY PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_policy_enabled CHECK (enabled_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_all CHECK (all_callers_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_channel CHECK (channel_policy_required_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_policy_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
) ENGINE=InnoDB;
ALTER TABLE OPS_OPERATION_POLICY COMMENT = 'CPF 업무 Operation ADM-owned 실행 정책';
CREATE INDEX ix_ops_operation_policy_enabled ON OPS_OPERATION_POLICY (enabled_yn, policy_version);

CREATE TABLE OPS_OPERATION_CALLER_POLICY (
    operation_id VARCHAR(160) NOT NULL,
    caller_system_code VARCHAR(32) NOT NULL,
    allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    policy_version BIGINT DEFAULT 1 NOT NULL,
    seed_source VARCHAR(80) NULL,
    seed_revision VARCHAR(120) NULL,
    seeded_at DATETIME(3) NULL,
    change_reason VARCHAR(500) NULL,
    created_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_OPS_OPERATION_CALLER_POLICY PRIMARY KEY (operation_id, caller_system_code),
    CONSTRAINT ck_ops_operation_caller_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_caller_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE
) ENGINE=InnoDB;
ALTER TABLE OPS_OPERATION_CALLER_POLICY COMMENT = 'CPF 업무 Operation Caller별 ADM-owned 정책';
CREATE INDEX ix_ops_operation_caller_caller ON OPS_OPERATION_CALLER_POLICY (caller_system_code, allowed_yn);



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
