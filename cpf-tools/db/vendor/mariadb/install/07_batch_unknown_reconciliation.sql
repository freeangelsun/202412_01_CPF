CREATE TABLE IF NOT EXISTS bat_reconciliation_audit (
    reconciliation_audit_id BIGINT NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(100) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_key VARCHAR(300) NOT NULL,
    from_status VARCHAR(40) NOT NULL,
    to_status VARCHAR(40) NOT NULL,
    requester_id VARCHAR(120) NOT NULL,
    approver_id VARCHAR(120) NOT NULL,
    reason_text VARCHAR(1000) NOT NULL,
    idempotency_key VARCHAR(200) NOT NULL,
    expected_attempt INTEGER,
    expected_version BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (reconciliation_audit_id),
    CONSTRAINT uq_bat_reconcile_idem UNIQUE (idempotency_key),
    CONSTRAINT ck_bat_reconcile_separation CHECK (requester_id <> approver_id),
    INDEX ix_bat_reconcile_entity(entity_type, entity_key, created_at)
) ENGINE=InnoDB;
