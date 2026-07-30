-- QA31-D008/D014: Gateway ingress/target path separation and shared replay nonce store.
CREATE TABLE cpf_gateway_control_nonce (
    audience VARCHAR2(160 CHAR) NOT NULL,
    key_id VARCHAR2(80 CHAR) NOT NULL,
    caller_id VARCHAR2(80 CHAR) NOT NULL,
    nonce VARCHAR2(160 CHAR) NOT NULL,
    claimed_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    CONSTRAINT pk_cpf_gateway_control_nonce PRIMARY KEY (audience, key_id, caller_id, nonce)
);
CREATE INDEX ix_cpf_gateway_control_nonce_expiry ON cpf_gateway_control_nonce (expires_at);

ALTER TABLE cpf_gateway_binding ADD (target_path VARCHAR2(500 CHAR));
UPDATE cpf_gateway_binding SET target_path = path_pattern WHERE target_path IS NULL;
ALTER TABLE cpf_gateway_binding MODIFY (target_path NOT NULL);

CREATE TABLE cpf_gateway_control_security_audit (
    event_id VARCHAR2(64 CHAR) NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    audience VARCHAR2(160 CHAR),
    key_id VARCHAR2(80 CHAR),
    caller_service VARCHAR2(80 CHAR),
    operator_id VARCHAR2(100 CHAR),
    http_method VARCHAR2(16 CHAR),
    request_target VARCHAR2(1000 CHAR),
    remote_address VARCHAR2(128 CHAR),
    result_code VARCHAR2(80 CHAR) NOT NULL,
    safe_message VARCHAR2(1000 CHAR),
    CONSTRAINT pk_cpf_gw_ctl_sec_audit PRIMARY KEY (event_id)
);
CREATE INDEX ix_cpf_gw_ctl_sec_audit_time ON cpf_gateway_control_security_audit (occurred_at);
