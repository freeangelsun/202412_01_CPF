-- CPF Gateway registry, binding, apply/test and transaction attempt ledger (V74)
CREATE TABLE cpf_gateway_server_group (
    server_group_id VARCHAR2(100) NOT NULL,
    group_name VARCHAR2(200) NOT NULL,
    environment_code VARCHAR2(50) NOT NULL,
    service_id VARCHAR2(100) NOT NULL,
    endpoint_code VARCHAR2(100) NOT NULL,
    target_protocol VARCHAR2(30) NOT NULL,
    load_balance_policy VARCHAR2(50) NOT NULL,
    hash_key_source VARCHAR2(200) DEFAULT '' NOT NULL,
    health_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    failover_group_id VARCHAR2(100) DEFAULT '' NOT NULL,
    group_status VARCHAR2(30) DEFAULT 'DRAFT' NOT NULL,
    direct_allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    created_by VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    row_version NUMBER(19) DEFAULT 1 NOT NULL,
    CONSTRAINT pk_gateway_server_group PRIMARY KEY (server_group_id)
);

CREATE TABLE cpf_gateway_server_group_member (
    server_group_id VARCHAR2(100) NOT NULL,
    instance_id VARCHAR2(100) NOT NULL,
    weight NUMBER(10) DEFAULT 1 NOT NULL,
    priority_no NUMBER(10) DEFAULT 0 NOT NULL,
    enabled_yn CHAR(1) DEFAULT 'Y' NOT NULL,
    effective_status VARCHAR2(30) DEFAULT 'UNKNOWN' NOT NULL,
    fencing_token NUMBER(19) DEFAULT 0 NOT NULL,
    created_by VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_gateway_server_group_member PRIMARY KEY (server_group_id, instance_id),
    CONSTRAINT fk_gwy_group_member_group FOREIGN KEY (server_group_id)
        REFERENCES cpf_gateway_server_group (server_group_id) ON DELETE CASCADE
);

CREATE TABLE cpf_gateway_binding (
    binding_id VARCHAR2(100) NOT NULL,
    route_id VARCHAR2(100) NOT NULL,
    environment_code VARCHAR2(50) NOT NULL,
    host_pattern VARCHAR2(300) NOT NULL,
    path_pattern VARCHAR2(500) NOT NULL,
    http_method VARCHAR2(20) DEFAULT '*' NOT NULL,
    api_version VARCHAR2(50) NOT NULL,
    ingress_protocol VARCHAR2(30) NOT NULL,
    target_protocol VARCHAR2(30) NOT NULL,
    service_id VARCHAR2(100) NOT NULL,
    server_group_id VARCHAR2(100) NOT NULL,
    route_version VARCHAR2(100) NOT NULL,
    tls_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    authentication_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    authorization_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    header_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    rate_limit_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    health_policy_id VARCHAR2(100) DEFAULT '' NOT NULL,
    connect_timeout_ms NUMBER(10) NOT NULL,
    response_timeout_ms NUMBER(10) NOT NULL,
    overall_timeout_ms NUMBER(10) NOT NULL,
    max_retry_count NUMBER(10) DEFAULT 0 NOT NULL,
    idempotent_yn CHAR(1) DEFAULT 'N' NOT NULL,
    failover_group_id VARCHAR2(100) DEFAULT '' NOT NULL,
    gateway_allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    direct_allowed_yn CHAR(1) DEFAULT 'N' NOT NULL,
    binding_status VARCHAR2(30) DEFAULT 'DRAFT' NOT NULL,
    approval_id VARCHAR2(100) DEFAULT '' NOT NULL,
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    created_by VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    row_version NUMBER(19) DEFAULT 1 NOT NULL,
    CONSTRAINT pk_gateway_binding PRIMARY KEY (binding_id),
    CONSTRAINT fk_gwy_binding_group FOREIGN KEY (server_group_id)
        REFERENCES cpf_gateway_server_group (server_group_id)
);

CREATE TABLE cpf_gateway_apply_status (
    binding_id VARCHAR2(100) NOT NULL,
    gateway_instance_id VARCHAR2(100) NOT NULL,
    expected_version VARCHAR2(100) NOT NULL,
    applied_version VARCHAR2(100) DEFAULT '' NOT NULL,
    apply_status VARCHAR2(30) NOT NULL,
    error_code VARCHAR2(100) DEFAULT '' NOT NULL,
    error_message VARCHAR2(1000) DEFAULT '' NOT NULL,
    acknowledged_at TIMESTAMP,
    last_seen_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_gateway_apply_status PRIMARY KEY (binding_id, gateway_instance_id),
    CONSTRAINT fk_gwy_apply_binding FOREIGN KEY (binding_id)
        REFERENCES cpf_gateway_binding (binding_id) ON DELETE CASCADE
);

CREATE TABLE cpf_gateway_connection_test (
    test_id VARCHAR2(100) NOT NULL,
    binding_id VARCHAR2(100) NOT NULL,
    gateway_instance_id VARCHAR2(100) DEFAULT '' NOT NULL,
    instance_id VARCHAR2(100) DEFAULT '' NOT NULL,
    test_type VARCHAR2(50) NOT NULL,
    test_status VARCHAR2(30) NOT NULL,
    failure_stage VARCHAR2(50) DEFAULT '' NOT NULL,
    duration_ms NUMBER(19) DEFAULT 0 NOT NULL,
    trace_id VARCHAR2(100) DEFAULT '' NOT NULL,
    operation_id VARCHAR2(100) DEFAULT '' NOT NULL,
    tested_at TIMESTAMP NOT NULL,
    tested_by VARCHAR2(100) NOT NULL,
    CONSTRAINT pk_gateway_connection_test PRIMARY KEY (test_id),
    CONSTRAINT fk_gwy_test_binding FOREIGN KEY (binding_id)
        REFERENCES cpf_gateway_binding (binding_id) ON DELETE CASCADE
);

CREATE TABLE cpf_gateway_transaction (
    gateway_transaction_id VARCHAR2(100) NOT NULL,
    transaction_id VARCHAR2(100) NOT NULL,
    trace_id VARCHAR2(100) NOT NULL,
    channel_id VARCHAR2(100) DEFAULT '' NOT NULL,
    source_ip VARCHAR2(100) DEFAULT '' NOT NULL,
    source_port NUMBER(10),
    gateway_instance_id VARCHAR2(100) NOT NULL,
    binding_id VARCHAR2(100) NOT NULL,
    route_id VARCHAR2(100) NOT NULL,
    route_version VARCHAR2(100) NOT NULL,
    server_group_id VARCHAR2(100) NOT NULL,
    final_instance_id VARCHAR2(100) DEFAULT '' NOT NULL,
    result_status VARCHAR2(30) NOT NULL,
    protocol_status VARCHAR2(30) DEFAULT '' NOT NULL,
    business_code VARCHAR2(100) DEFAULT '' NOT NULL,
    failure_stage VARCHAR2(50) DEFAULT '' NOT NULL,
    unknown_yn CHAR(1) DEFAULT 'N' NOT NULL,
    total_duration_ms NUMBER(19) DEFAULT 0 NOT NULL,
    request_size NUMBER(19) DEFAULT 0 NOT NULL,
    response_size NUMBER(19) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_gateway_transaction PRIMARY KEY (gateway_transaction_id)
);

CREATE TABLE cpf_gateway_attempt (
    attempt_id VARCHAR2(100) NOT NULL,
    gateway_transaction_id VARCHAR2(100) NOT NULL,
    attempt_no NUMBER(10) NOT NULL,
    instance_id VARCHAR2(100) NOT NULL,
    target_host VARCHAR2(300) DEFAULT '' NOT NULL,
    target_port NUMBER(10),
    target_protocol VARCHAR2(30) NOT NULL,
    connect_duration_ms NUMBER(19) DEFAULT 0 NOT NULL,
    response_duration_ms NUMBER(19) DEFAULT 0 NOT NULL,
    attempt_status VARCHAR2(30) NOT NULL,
    protocol_status VARCHAR2(30) DEFAULT '' NOT NULL,
    failure_code VARCHAR2(100) DEFAULT '' NOT NULL,
    failure_message VARCHAR2(1000) DEFAULT '' NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    CONSTRAINT pk_gateway_attempt PRIMARY KEY (attempt_id),
    CONSTRAINT fk_gwy_attempt_tx FOREIGN KEY (gateway_transaction_id)
        REFERENCES cpf_gateway_transaction (gateway_transaction_id) ON DELETE CASCADE
);

CREATE INDEX idx_gwy_group_service ON cpf_gateway_server_group (environment_code, service_id, group_status);
CREATE INDEX idx_gwy_binding_route ON cpf_gateway_binding (environment_code, route_id, binding_status);
CREATE UNIQUE INDEX uq_gwy_binding_key ON cpf_gateway_binding (environment_code, host_pattern, path_pattern, http_method, api_version, route_version);
CREATE INDEX idx_gwy_apply_status ON cpf_gateway_apply_status (apply_status, last_seen_at);
CREATE INDEX idx_gwy_test_binding ON cpf_gateway_connection_test (binding_id, tested_at);
CREATE INDEX idx_gwy_tx_trace ON cpf_gateway_transaction (transaction_id, trace_id, created_at);
CREATE INDEX idx_gwy_tx_route ON cpf_gateway_transaction (route_id, result_status, created_at);
CREATE UNIQUE INDEX uq_gwy_attempt_no ON cpf_gateway_attempt (gateway_transaction_id, attempt_no);
