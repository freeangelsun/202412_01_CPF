-- R9 ADM control plane: incident lifecycle and maintenance command audit
CREATE TABLE IF NOT EXISTS adm_incident (
    incident_id BIGINT NOT NULL AUTO_INCREMENT,
    incident_no VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    title VARCHAR(300) NOT NULL,
    summary VARCHAR(2000) NULL,
    source_type VARCHAR(40) NOT NULL DEFAULT 'MANUAL',
    source_id VARCHAR(200) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    acknowledged_at DATETIME(3) NULL,
    mitigated_at DATETIME(3) NULL,
    resolved_at DATETIME(3) NULL,
    reason VARCHAR(1000) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (incident_id),
    UNIQUE KEY uk_adm_incident_no (incident_no),
    KEY idx_adm_incident_status (status, severity, detected_at),
    KEY idx_adm_incident_source (source_type, source_id)
);

CREATE TABLE IF NOT EXISTS adm_maintenance_action (
    action_id BIGINT NOT NULL AUTO_INCREMENT,
    service_id VARCHAR(100) NOT NULL,
    endpoint_code VARCHAR(100) NOT NULL,
    instance_id VARCHAR(150) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_status VARCHAR(40) NULL,
    after_status VARCHAR(40) NULL,
    result_status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    result_detail TEXT NULL,
    PRIMARY KEY (action_id),
    KEY idx_adm_maintenance_target (service_id, endpoint_code, instance_id, requested_at),
    KEY idx_adm_maintenance_result (result_status, requested_at)
);
