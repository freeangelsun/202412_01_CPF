-- QA31-D008/D014: Gateway ingress/target path separation and shared replay nonce store.
CREATE TABLE cpf_gateway_control_nonce (
    audience VARCHAR(160) NOT NULL,
    key_id VARCHAR(80) NOT NULL,
    caller_id VARCHAR(80) NOT NULL,
    nonce VARCHAR(160) NOT NULL,
    claimed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP(3) NOT NULL,
    CONSTRAINT pk_cpf_gateway_control_nonce PRIMARY KEY (audience, key_id, caller_id, nonce)
);
CREATE INDEX ix_cpf_gateway_control_nonce_expiry ON cpf_gateway_control_nonce (expires_at);

ALTER TABLE cpf_gateway_binding ADD COLUMN target_path VARCHAR(500);
UPDATE cpf_gateway_binding SET target_path = path_pattern WHERE target_path IS NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN target_path SET NOT NULL;

CREATE TABLE cpf_gateway_control_security_audit (
    event_id VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    audience VARCHAR(160),
    key_id VARCHAR(80),
    caller_service VARCHAR(80),
    operator_id VARCHAR(100),
    http_method VARCHAR(16),
    request_target VARCHAR(1000),
    remote_address VARCHAR(128),
    result_code VARCHAR(80) NOT NULL,
    safe_message VARCHAR(1000),
    CONSTRAINT pk_cpf_gw_ctl_sec_audit PRIMARY KEY (event_id)
);
CREATE INDEX ix_cpf_gw_ctl_sec_audit_time ON cpf_gateway_control_security_audit (occurred_at);
