-- V40 CPF Saga compensation/manual recovery runtime


-- R8 Saga compensation/manual recovery durable runtime
CREATE TABLE IF NOT EXISTS cpf_saga_execution (
    saga_id VARCHAR(100) NOT NULL,
    saga_type VARCHAR(100) NOT NULL,
    business_key VARCHAR(200) NULL,
    transaction_id VARCHAR(100) NULL,
    saga_status VARCHAR(40) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    error_message VARCHAR(2000) NULL,
    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    completed_at DATETIME(3) NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (saga_id),
    KEY idx_cpf_saga_status (saga_status, updated_at),
    KEY idx_cpf_saga_business (saga_type, business_key)
);

CREATE TABLE IF NOT EXISTS cpf_saga_step_execution (
    saga_id VARCHAR(100) NOT NULL,
    step_no INT NOT NULL,
    step_id VARCHAR(100) NOT NULL,
    step_status VARCHAR(40) NOT NULL,
    result_code VARCHAR(100) NULL,
    result_snapshot TEXT NULL,
    error_message VARCHAR(2000) NULL,
    execute_attempts INT NOT NULL DEFAULT 0,
    compensation_attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (saga_id, step_no),
    KEY idx_cpf_saga_step_status (step_status, updated_at),
    CONSTRAINT fk_cpf_saga_step_execution FOREIGN KEY (saga_id) REFERENCES cpf_saga_execution(saga_id)
);

CREATE TABLE IF NOT EXISTS cpf_saga_manual_action (
    action_id VARCHAR(36) NOT NULL,
    saga_id VARCHAR(100) NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    operator_id VARCHAR(100) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    before_status VARCHAR(40) NULL,
    after_status VARCHAR(40) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (action_id),
    KEY idx_cpf_saga_manual (saga_id, created_at),
    CONSTRAINT fk_cpf_saga_manual_action FOREIGN KEY (saga_id) REFERENCES cpf_saga_execution(saga_id)
);
