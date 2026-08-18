-- Central Managed Server Registry: stable server identity + existing runtime instance association.
CREATE TABLE OPS_MANAGED_SERVER (
    managed_server_id VARCHAR(80) NOT NULL,
    server_name VARCHAR(150) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    hostname VARCHAR(200) NULL,
    management_identity VARCHAR(200) NULL,
    environment_code VARCHAR(40) DEFAULT 'default' NOT NULL,
    server_group VARCHAR(100) NULL,
    zone_code VARCHAR(60) NULL,
    location VARCHAR(200) NULL,
    description VARCHAR(500) NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    status VARCHAR(30) DEFAULT 'REGISTERED' NOT NULL,
    tags_json TEXT NULL,
    registered_at TIMESTAMP(3) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    registered_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    row_version BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'CPF' NOT NULL,
    CONSTRAINT PK_OPS_MANAGED_SERVER PRIMARY KEY (managed_server_id),
    CONSTRAINT uk_ops_managed_server_management_identity UNIQUE (management_identity),
    CONSTRAINT ck_ops_managed_server_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_managed_server_status CHECK (status IN ('PENDING','REGISTERED','ACTIVE','DEGRADED','DISABLED','DECOMMISSIONED','UNKNOWN','MAINTENANCE'))
);
CREATE INDEX ix_ops_managed_server_name ON OPS_MANAGED_SERVER (server_name);
CREATE INDEX ix_ops_managed_server_env_status ON OPS_MANAGED_SERVER (environment_code, status, enabled_yn);
CREATE INDEX ix_ops_managed_server_group ON OPS_MANAGED_SERVER (server_group, environment_code);
CREATE INDEX ix_ops_managed_server_hostname ON OPS_MANAGED_SERVER (hostname, environment_code);

ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN managed_server_id VARCHAR(80) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN system_code VARCHAR(16) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN application_name VARCHAR(150) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN application_role VARCHAR(80) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN runtime_hostname VARCHAR(200) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN process_id VARCHAR(80) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN java_version VARCHAR(80) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN cpf_version VARCHAR(80) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN application_version VARCHAR(100) NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD COLUMN started_at TIMESTAMP(3) WITHOUT TIME ZONE NULL;
ALTER TABLE OPS_SERVICE_INSTANCE ADD CONSTRAINT fk_cpf_service_instance_managed_server FOREIGN KEY (managed_server_id) REFERENCES OPS_MANAGED_SERVER (managed_server_id);
CREATE INDEX ix_cpf_service_instance_managed_server ON OPS_SERVICE_INSTANCE (managed_server_id, instance_status, last_heartbeat_at);
