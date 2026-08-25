-- CPF_LOGICAL_DATABASE=mbwDB
-- Append-only: 승인과 실제 업무 적용 결과를 분리 추적합니다.
USE mbwDB;
CREATE TABLE MBW_APPROVAL_EXECUTION (
    approval_id BIGINT NOT NULL,
    command_request_id VARCHAR(120) NOT NULL,
    owner_action VARCHAR(80) NOT NULL,
    execution_status VARCHAR(30) DEFAULT 'PENDING' NOT NULL,
    owner_result_code VARCHAR(80) NULL,
    owner_result_message VARCHAR(1000) NULL,
    started_at DATETIME(3) NULL,
    completed_at DATETIME(3) NULL,
    recovery_required_yn CHAR(1) DEFAULT 'N' NOT NULL,
    fence_token BIGINT DEFAULT 0 NOT NULL,
    approved_by VARCHAR(100) NOT NULL,
    transaction_id CHAR(34) NULL,
    created_by VARCHAR(100) DEFAULT 'SYSTEM' NOT NULL,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    updated_by VARCHAR(100) DEFAULT 'SYSTEM' NOT NULL,
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT PK_MBW_APPROVAL_EXECUTION PRIMARY KEY (approval_id),
    CONSTRAINT uk_mbw_approval_execution_command UNIQUE (command_request_id),
    CONSTRAINT ck_mbw_approval_execution_status CHECK (execution_status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','UNKNOWN','RECONCILING','RECOVERED')),
    CONSTRAINT ck_mbw_approval_execution_recovery CHECK (recovery_required_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_approval_execution_fence CHECK (fence_token >= 0),
    CONSTRAINT ck_mbw_approval_execution_time CHECK (completed_at IS NULL OR started_at IS NULL OR completed_at >= started_at),
    CONSTRAINT fk_mbw_approval_execution_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id)
) ENGINE=InnoDB;
ALTER TABLE MBW_APPROVAL_EXECUTION COMMENT = 'Backoffice 결재 승인 후 실제 업무 Owner 실행 상태';
CREATE INDEX ix_mbw_approval_execution_status ON MBW_APPROVAL_EXECUTION (execution_status, recovery_required_yn, updated_at);
