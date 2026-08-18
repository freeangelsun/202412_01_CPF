-- Central Managed Server Registry: stable server identity + existing runtime instance association.
CREATE TABLE OPS_MANAGED_SERVER (
    managed_server_id VARCHAR2(80 CHAR) NOT NULL,
    server_name VARCHAR2(150 CHAR) NOT NULL,
    display_name VARCHAR2(200 CHAR) NOT NULL,
    hostname VARCHAR2(200 CHAR) NULL,
    management_identity VARCHAR2(200 CHAR) NULL,
    environment_code VARCHAR2(40 CHAR) DEFAULT 'default' NOT NULL,
    server_group VARCHAR2(100 CHAR) NULL,
    zone_code VARCHAR2(60 CHAR) NULL,
    location VARCHAR2(200 CHAR) NULL,
    description VARCHAR2(500 CHAR) NULL,
    enabled_yn CHAR(1 CHAR) DEFAULT 'Y' NOT NULL,
    status VARCHAR2(30 CHAR) DEFAULT 'REGISTERED' NOT NULL,
    tags_json CLOB NULL,
    registered_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    registered_by VARCHAR2(100 CHAR) DEFAULT 'CPF' NOT NULL,
    row_version NUMBER(19) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100 CHAR) DEFAULT 'CPF' NOT NULL,
    CONSTRAINT PK_OPS_MANAGED_SERVER PRIMARY KEY (managed_server_id),
    CONSTRAINT uk_ops_managed_server_management_identity UNIQUE (management_identity),
    CONSTRAINT ck_ops_managed_server_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_managed_server_status CHECK (status IN ('PENDING','REGISTERED','ACTIVE','DEGRADED','DISABLED','DECOMMISSIONED','UNKNOWN','MAINTENANCE'))
);
CREATE INDEX ix_ops_managed_server_name ON OPS_MANAGED_SERVER (server_name);
CREATE INDEX ix_ops_managed_server_env_status ON OPS_MANAGED_SERVER (environment_code, status, enabled_yn);
CREATE INDEX ix_ops_managed_server_group ON OPS_MANAGED_SERVER (server_group, environment_code);
CREATE INDEX ix_ops_managed_server_hostname ON OPS_MANAGED_SERVER (hostname, environment_code);

ALTER TABLE OPS_SERVICE_INSTANCE ADD (
    managed_server_id VARCHAR2(80 CHAR) NULL,
    system_code VARCHAR2(16 CHAR) NULL,
    application_name VARCHAR2(150 CHAR) NULL,
    application_role VARCHAR2(80 CHAR) NULL,
    runtime_hostname VARCHAR2(200 CHAR) NULL,
    process_id VARCHAR2(80 CHAR) NULL,
    java_version VARCHAR2(80 CHAR) NULL,
    cpf_version VARCHAR2(80 CHAR) NULL,
    application_version VARCHAR2(100 CHAR) NULL,
    started_at TIMESTAMP(3) NULL
);
ALTER TABLE OPS_SERVICE_INSTANCE ADD CONSTRAINT fk_cpf_service_instance_managed_server FOREIGN KEY (managed_server_id) REFERENCES OPS_MANAGED_SERVER (managed_server_id);
CREATE INDEX ix_cpf_service_instance_managed_server ON OPS_SERVICE_INSTANCE (managed_server_id, instance_status, last_heartbeat_at);
