-- CPF_LOGICAL_DATABASE=mbwDB
-- Append-only: 승인과 실제 업무 적용 결과를 분리 추적합니다.
CREATE TABLE MBW_APPROVAL_EXECUTION (
    approval_id NUMBER(19) NOT NULL,
    command_request_id VARCHAR2(120 CHAR) NOT NULL,
    owner_action VARCHAR2(80 CHAR) NOT NULL,
    execution_status VARCHAR2(30 CHAR) DEFAULT 'PENDING' NOT NULL,
    owner_result_code VARCHAR2(80 CHAR) NULL,
    owner_result_message VARCHAR2(1000 CHAR) NULL,
    started_at TIMESTAMP(3) NULL,
    completed_at TIMESTAMP(3) NULL,
    recovery_required_yn CHAR(1 CHAR) DEFAULT 'N' NOT NULL,
    fence_token NUMBER(19) DEFAULT 0 NOT NULL,
    approved_by VARCHAR2(100 CHAR) NOT NULL,
    transaction_id CHAR(34 CHAR) NULL,
    created_by VARCHAR2(100 CHAR) DEFAULT 'SYSTEM' NOT NULL,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    updated_by VARCHAR2(100 CHAR) DEFAULT 'SYSTEM' NOT NULL,
    updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    CONSTRAINT PK_MBW_APPROVAL_EXECUTION PRIMARY KEY (approval_id),
    CONSTRAINT uk_mbw_approval_execution_command UNIQUE (command_request_id),
    CONSTRAINT ck_mbw_approval_execution_status CHECK (execution_status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','UNKNOWN','RECONCILING','RECOVERED')),
    CONSTRAINT ck_mbw_approval_execution_recovery CHECK (recovery_required_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_approval_execution_fence CHECK (fence_token >= 0),
    CONSTRAINT ck_mbw_approval_execution_time CHECK (completed_at IS NULL OR started_at IS NULL OR completed_at >= started_at),
    CONSTRAINT fk_mbw_approval_execution_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id)
);
COMMENT ON TABLE MBW_APPROVAL_EXECUTION IS 'Backoffice 결재 승인 후 실제 업무 Owner 실행 상태';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.approval_id IS '결재 문서 순번';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.command_request_id IS 'Owner 실행 멱등 요청 ID';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.owner_action IS '실제 업무 Owner Action';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.execution_status IS 'PENDING/RUNNING/SUCCEEDED/FAILED/UNKNOWN/RECONCILING/RECOVERED';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.owner_result_code IS 'Owner 실행 결과 코드';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.owner_result_message IS '마스킹된 Owner 실행 결과 메시지';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.started_at IS '실제 실행 시작 시각';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.completed_at IS '실제 실행 종료 시각';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.recovery_required_yn IS '결과불명/복구 필요 여부';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.fence_token IS '실행/Reconcile fencing token';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.approved_by IS '최종 승인 처리 운영자';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.transaction_id IS '승인/실행 상관관계 transactionId';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.created_by IS '등록자';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.created_at IS '등록일시';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.updated_by IS '수정자';
COMMENT ON COLUMN MBW_APPROVAL_EXECUTION.updated_at IS '수정일시';
CREATE INDEX ix_mbw_approval_execution_status ON MBW_APPROVAL_EXECUTION (execution_status, recovery_required_yn, updated_at);
