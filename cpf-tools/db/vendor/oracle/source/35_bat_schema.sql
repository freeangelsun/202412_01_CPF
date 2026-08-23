-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=oracle
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=batDB
CREATE TABLE bat_deployment_cell (
    cell_id VARCHAR2(120 CHAR),
    environment_id VARCHAR2(80 CHAR) NOT NULL,
    runtime_role VARCHAR2(40 CHAR) NOT NULL,
    service_id VARCHAR2(120 CHAR) NOT NULL,
    manifest_version VARCHAR2(80 CHAR) NOT NULL,
    manifest_hash VARCHAR2(128 CHAR) NOT NULL,
    desired_state VARCHAR2(32 CHAR) NOT NULL,
    desired_instance_count NUMBER(10) NOT NULL DEFAULT 1,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_deployment_cell PRIMARY KEY (cell_id)
);
COMMENT ON TABLE bat_deployment_cell IS 'BAT deployment cell desired state';
COMMENT ON COLUMN bat_deployment_cell.cell_id IS 'Deployment cell identifier';
COMMENT ON COLUMN bat_deployment_cell.environment_id IS 'Target environment identifier';
COMMENT ON COLUMN bat_deployment_cell.runtime_role IS 'Target runtime role';
COMMENT ON COLUMN bat_deployment_cell.service_id IS 'Target service identifier';
COMMENT ON COLUMN bat_deployment_cell.manifest_version IS 'Desired manifest version';
COMMENT ON COLUMN bat_deployment_cell.manifest_hash IS 'Desired manifest checksum';
COMMENT ON COLUMN bat_deployment_cell.desired_state IS 'Desired cell state';
COMMENT ON COLUMN bat_deployment_cell.desired_instance_count IS 'Desired runtime instance count';
COMMENT ON COLUMN bat_deployment_cell.row_version IS 'Optimistic locking version';
COMMENT ON COLUMN bat_deployment_cell.created_at IS 'Cell registration time';
COMMENT ON COLUMN bat_deployment_cell.updated_at IS 'Last desired-state update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_deployment_cell BEFORE UPDATE ON bat_deployment_cell FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_deployment_lock (
    cell_id VARCHAR2(120 CHAR),
    owner_deployment_id VARCHAR2(80 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    locked_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_bat_deployment_lock PRIMARY KEY (cell_id)
);
COMMENT ON TABLE bat_deployment_lock IS 'BAT deployment cell lease lock';
COMMENT ON COLUMN bat_deployment_lock.cell_id IS 'Locked deployment cell identifier';
COMMENT ON COLUMN bat_deployment_lock.owner_deployment_id IS 'Lock owner deployment identifier';
COMMENT ON COLUMN bat_deployment_lock.fencing_token IS 'Monotonic deployment fencing token';
COMMENT ON COLUMN bat_deployment_lock.locked_at IS 'Lock acquisition time';
COMMENT ON COLUMN bat_deployment_lock.expires_at IS 'Lock expiry time';

CREATE TABLE bat_deployment_plan (
    plan_id VARCHAR2(80 CHAR),
    cell_id VARCHAR2(120 CHAR) NOT NULL,
    manifest_json CLOB NOT NULL,
    manifest_hash VARCHAR2(128 CHAR) NOT NULL,
    requested_by VARCHAR2(120 CHAR) NOT NULL,
    reason_text VARCHAR2(1000 CHAR) NOT NULL,
    plan_state VARCHAR2(40 CHAR) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_deployment_plan PRIMARY KEY (plan_id)
);
COMMENT ON TABLE bat_deployment_plan IS 'BAT deployment plan';
COMMENT ON COLUMN bat_deployment_plan.plan_id IS 'Deployment plan identifier';
COMMENT ON COLUMN bat_deployment_plan.cell_id IS 'Target deployment cell identifier';
COMMENT ON COLUMN bat_deployment_plan.manifest_json IS 'Immutable deployment manifest snapshot';
COMMENT ON COLUMN bat_deployment_plan.manifest_hash IS 'Deployment manifest checksum';
COMMENT ON COLUMN bat_deployment_plan.requested_by IS 'Plan requester';
COMMENT ON COLUMN bat_deployment_plan.reason_text IS 'Mandatory deployment reason';
COMMENT ON COLUMN bat_deployment_plan.plan_state IS 'Deployment plan lifecycle state';
COMMENT ON COLUMN bat_deployment_plan.created_at IS 'Plan request time';
COMMENT ON COLUMN bat_deployment_plan.updated_at IS 'Last plan state update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_deployment_plan BEFORE UPDATE ON bat_deployment_plan FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_instance (
    instance_id VARCHAR2(100 CHAR) NOT NULL,
    instance_name VARCHAR2(150 CHAR) NOT NULL,
    host_name VARCHAR2(150 CHAR),
    server_port NUMBER(10),
    active_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    last_heartbeat_at TIMESTAMP(3),
    description VARCHAR2(500 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_instance PRIMARY KEY (instance_id)
);
CREATE INDEX ix_bat_instance_active ON bat_instance (active_yn, last_heartbeat_at);
COMMENT ON TABLE bat_instance IS 'BAT 배치 서버 인스턴스';
COMMENT ON COLUMN bat_instance.instance_id IS '배치 인스턴스 ID';
COMMENT ON COLUMN bat_instance.instance_name IS '배치 인스턴스명';
COMMENT ON COLUMN bat_instance.host_name IS '호스트명';
COMMENT ON COLUMN bat_instance.server_port IS '서버 포트';
COMMENT ON COLUMN bat_instance.active_yn IS '활성 여부';
COMMENT ON COLUMN bat_instance.last_heartbeat_at IS '마지막 heartbeat 일시';
COMMENT ON COLUMN bat_instance.description IS '인스턴스 설명';
COMMENT ON COLUMN bat_instance.created_by IS '등록자';
COMMENT ON COLUMN bat_instance.created_at IS '등록일시';
COMMENT ON COLUMN bat_instance.updated_by IS '수정자';
COMMENT ON COLUMN bat_instance.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_instance BEFORE UPDATE ON bat_instance FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_job_definition_audit (
    audit_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    action_code VARCHAR2(40 CHAR) NOT NULL,
    from_state VARCHAR2(20 CHAR),
    to_state VARCHAR2(20 CHAR),
    reason VARCHAR2(1000 CHAR) NOT NULL,
    operator_id VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    requested_by VARCHAR2(100 CHAR),
    approval_request_id VARCHAR2(120 CHAR),
    transaction_id CHAR(34 CHAR),
    trace_id VARCHAR2(64 CHAR),
    before_json CLOB,
    after_json CLOB,
    CONSTRAINT pk_bat_job_definition_audit PRIMARY KEY (audit_id)
);
CREATE INDEX idx_bat_job_def_audit ON bat_job_definition_audit (job_id, definition_version, created_at);
CREATE INDEX ix_bat_job_definition_audit_approval ON bat_job_definition_audit (approval_request_id, created_at);
COMMENT ON TABLE bat_job_definition_audit IS 'BAT Job Definition 승인·상태 감사';
COMMENT ON COLUMN bat_job_definition_audit.audit_id IS '감사 ID';
COMMENT ON COLUMN bat_job_definition_audit.job_id IS 'Job ID';
COMMENT ON COLUMN bat_job_definition_audit.definition_version IS 'Definition Version';
COMMENT ON COLUMN bat_job_definition_audit.action_code IS '행위';
COMMENT ON COLUMN bat_job_definition_audit.from_state IS '이전 상태';
COMMENT ON COLUMN bat_job_definition_audit.to_state IS '다음 상태';
COMMENT ON COLUMN bat_job_definition_audit.reason IS '사유';
COMMENT ON COLUMN bat_job_definition_audit.operator_id IS '운영자';
COMMENT ON COLUMN bat_job_definition_audit.created_at IS '발생시각';
COMMENT ON COLUMN bat_job_definition_audit.requested_by IS '승인 대상 변경 요청자';
COMMENT ON COLUMN bat_job_definition_audit.approval_request_id IS '검증된 승인 요청 식별자';
COMMENT ON COLUMN bat_job_definition_audit.transaction_id IS '운영 명령 Transaction ID';
COMMENT ON COLUMN bat_job_definition_audit.trace_id IS '분산 추적 Trace ID';
COMMENT ON COLUMN bat_job_definition_audit.before_json IS '마스킹된 변경 전 Definition';
COMMENT ON COLUMN bat_job_definition_audit.after_json IS '마스킹된 변경 후 Definition';

CREATE TABLE bat_job_definition_version (
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    job_name VARCHAR2(200 CHAR) NOT NULL,
    executor_type VARCHAR2(40 CHAR) NOT NULL,
    definition_state VARCHAR2(20 CHAR) NOT NULL,
    owner_domain VARCHAR2(80 CHAR) NOT NULL,
    description VARCHAR2(1000 CHAR),
    trigger_type VARCHAR2(30 CHAR) NOT NULL,
    trigger_expression VARCHAR2(500 CHAR),
    timezone_id VARCHAR2(60 CHAR) NOT NULL DEFAULT 'Asia/Seoul',
    misfire_policy VARCHAR2(30 CHAR) NOT NULL,
    agent_pool VARCHAR2(100 CHAR) NOT NULL,
    zone_id VARCHAR2(80 CHAR),
    max_concurrency NUMBER(10) NOT NULL DEFAULT 1,
    timeout_seconds NUMBER(19) NOT NULL DEFAULT 3600,
    restartable_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    max_attempts NUMBER(10) NOT NULL DEFAULT 1,
    initial_backoff_seconds NUMBER(19) NOT NULL DEFAULT 0,
    backoff_multiplier DECIMAL(10,4) NOT NULL DEFAULT 1,
    max_backoff_seconds NUMBER(19) NOT NULL DEFAULT 0,
    skip_limit NUMBER(10) NOT NULL DEFAULT 0,
    unknown_result_policy VARCHAR2(30 CHAR) NOT NULL,
    compensation_reference VARCHAR2(200 CHAR),
    alert_delay_seconds NUMBER(19) NOT NULL DEFAULT 0,
    sla_seconds NUMBER(19) NOT NULL DEFAULT 0,
    notify_failure_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    notify_missed_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    executor_reference VARCHAR2(300 CHAR) NOT NULL,
    definition_json CLOB NOT NULL,
    checksum VARCHAR2(128 CHAR),
    effective_from TIMESTAMP,
    effective_until TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 1,
    created_by VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_job_definition_version PRIMARY KEY (job_id, definition_version),
    CONSTRAINT ck_bat_job_def_state CHECK (definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED','RETIRED'))
);
CREATE INDEX idx_bat_job_def_state ON bat_job_definition_version (definition_state, updated_at);
CREATE INDEX idx_bat_job_def_owner ON bat_job_definition_version (owner_domain, job_id);
COMMENT ON TABLE bat_job_definition_version IS 'BAT Versioned Job Definition 정본';
COMMENT ON COLUMN bat_job_definition_version.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_job_definition_version.definition_version IS '불변 Definition Version';
COMMENT ON COLUMN bat_job_definition_version.job_name IS '배치 Job 이름';
COMMENT ON COLUMN bat_job_definition_version.executor_type IS 'Executor 유형';
COMMENT ON COLUMN bat_job_definition_version.definition_state IS 'Definition 상태';
COMMENT ON COLUMN bat_job_definition_version.owner_domain IS '소유 업무영역';
COMMENT ON COLUMN bat_job_definition_version.description IS '설명';
COMMENT ON COLUMN bat_job_definition_version.trigger_type IS 'Trigger 유형';
COMMENT ON COLUMN bat_job_definition_version.trigger_expression IS 'Trigger 조건';
COMMENT ON COLUMN bat_job_definition_version.timezone_id IS 'Timezone';
COMMENT ON COLUMN bat_job_definition_version.misfire_policy IS 'Misfire 정책';
COMMENT ON COLUMN bat_job_definition_version.agent_pool IS 'Agent Pool';
COMMENT ON COLUMN bat_job_definition_version.zone_id IS '실행 Zone';
COMMENT ON COLUMN bat_job_definition_version.max_concurrency IS '최대 동시 실행';
COMMENT ON COLUMN bat_job_definition_version.timeout_seconds IS 'Timeout 초';
COMMENT ON COLUMN bat_job_definition_version.restartable_yn IS '재시작 가능 여부';
COMMENT ON COLUMN bat_job_definition_version.max_attempts IS '최대 시도';
COMMENT ON COLUMN bat_job_definition_version.initial_backoff_seconds IS '초기 Backoff';
COMMENT ON COLUMN bat_job_definition_version.backoff_multiplier IS 'Backoff 배수';
COMMENT ON COLUMN bat_job_definition_version.max_backoff_seconds IS '최대 Backoff';
COMMENT ON COLUMN bat_job_definition_version.skip_limit IS 'Skip 허용';
COMMENT ON COLUMN bat_job_definition_version.unknown_result_policy IS '결과 불명 처리 정책';
COMMENT ON COLUMN bat_job_definition_version.compensation_reference IS '보상 처리 참조';
COMMENT ON COLUMN bat_job_definition_version.alert_delay_seconds IS '지연 알림';
COMMENT ON COLUMN bat_job_definition_version.sla_seconds IS 'SLA';
COMMENT ON COLUMN bat_job_definition_version.notify_failure_yn IS '실패 알림';
COMMENT ON COLUMN bat_job_definition_version.notify_missed_yn IS '미실행 알림';
COMMENT ON COLUMN bat_job_definition_version.executor_reference IS 'Executor 참조';
COMMENT ON COLUMN bat_job_definition_version.definition_json IS 'Definition JSON';
COMMENT ON COLUMN bat_job_definition_version.checksum IS 'Checksum';
COMMENT ON COLUMN bat_job_definition_version.effective_from IS '시행 시작';
COMMENT ON COLUMN bat_job_definition_version.effective_until IS '시행 종료';
COMMENT ON COLUMN bat_job_definition_version.row_version IS '낙관적 잠금';
COMMENT ON COLUMN bat_job_definition_version.created_by IS '등록자';
COMMENT ON COLUMN bat_job_definition_version.created_at IS '등록일시';
COMMENT ON COLUMN bat_job_definition_version.updated_by IS '수정자';
COMMENT ON COLUMN bat_job_definition_version.updated_at IS '수정일시';

CREATE TABLE bat_job_pack (
    job_pack_id VARCHAR2(120 CHAR) NOT NULL,
    owner_domain VARCHAR2(20 CHAR) NOT NULL,
    artifact_coordinate VARCHAR2(240 CHAR) NOT NULL,
    artifact_version VARCHAR2(80 CHAR) NOT NULL,
    artifact_checksum VARCHAR2(128 CHAR),
    signature_present_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    platform_range VARCHAR2(120 CHAR),
    manifest_json CLOB NOT NULL,
    last_registered_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_bat_job_pack PRIMARY KEY (job_pack_id)
);
CREATE INDEX ix_bat_job_pack_owner ON bat_job_pack (owner_domain, artifact_version);
COMMENT ON TABLE bat_job_pack IS 'BAT external job-pack catalog';
COMMENT ON COLUMN bat_job_pack.job_pack_id IS 'Job-pack identifier';
COMMENT ON COLUMN bat_job_pack.owner_domain IS 'Owning domain SystemCode';
COMMENT ON COLUMN bat_job_pack.artifact_coordinate IS 'Job-pack artifact coordinate';
COMMENT ON COLUMN bat_job_pack.artifact_version IS 'Job-pack artifact version';
COMMENT ON COLUMN bat_job_pack.artifact_checksum IS 'Job-pack artifact checksum';
COMMENT ON COLUMN bat_job_pack.signature_present_yn IS 'Artifact signature presence flag';
COMMENT ON COLUMN bat_job_pack.platform_range IS 'Compatible CPF platform range';
COMMENT ON COLUMN bat_job_pack.manifest_json IS 'Validated job-pack manifest';
COMMENT ON COLUMN bat_job_pack.last_registered_at IS 'Last catalog registration time';

CREATE TABLE bat_lock (
    lock_key VARCHAR2(200 CHAR) NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    job_parameters_hash VARCHAR2(128 CHAR) NOT NULL,
    owner_id VARCHAR2(100 CHAR) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expire_at TIMESTAMP(3) NOT NULL,
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    CONSTRAINT pk_bat_lock PRIMARY KEY (lock_key)
);
CREATE INDEX ix_bat_lock_job ON bat_lock (job_id, job_parameters_hash);
CREATE INDEX ix_bat_lock_expire ON bat_lock (expire_at);
COMMENT ON TABLE bat_lock IS 'BAT 배치 중복 실행 방지 잠금';
COMMENT ON COLUMN bat_lock.lock_key IS '배치 잠금 키';
COMMENT ON COLUMN bat_lock.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_lock.job_parameters_hash IS 'Job 파라미터 해시';
COMMENT ON COLUMN bat_lock.owner_id IS '잠금 소유자';
COMMENT ON COLUMN bat_lock.locked_at IS '잠금 획득 일시';
COMMENT ON COLUMN bat_lock.expire_at IS '잠금 만료 일시';
COMMENT ON COLUMN bat_lock.created_by IS '등록자';
COMMENT ON COLUMN bat_lock.created_at IS '등록일시';
COMMENT ON COLUMN bat_lock.updated_by IS '수정자';
COMMENT ON COLUMN bat_lock.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_lock BEFORE UPDATE ON bat_lock FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_on_demand_request (
    execution_request_id VARCHAR2(36 CHAR) NOT NULL,
    standard_batch_id CHAR(10 CHAR) NOT NULL,
    idempotency_key VARCHAR2(120 CHAR) NOT NULL,
    transaction_id CHAR(34 CHAR) NOT NULL,
    business_date CHAR(8 CHAR) NOT NULL,
    request_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'REQUESTED',
    parameters_json CLOB,
    request_reason VARCHAR2(500 CHAR) NOT NULL,
    request_user VARCHAR2(100 CHAR) NOT NULL,
    cpf_execution_id NUMBER(19),
    spring_batch_execution_id NUMBER(19),
    result_json CLOB,
    failure_code VARCHAR2(100 CHAR),
    failure_message VARCHAR2(1000 CHAR),
    requested_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    completed_at TIMESTAMP(3),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_on_demand_request PRIMARY KEY (execution_request_id),
    CONSTRAINT uk_bat_on_demand_idempotency UNIQUE (standard_batch_id, idempotency_key),
    CONSTRAINT ck_bat_on_demand_id CHECK (REGEXP_LIKE(standard_batch_id, '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$') AND SUBSTR(standard_batch_id, -4) <> '0000'),
    CONSTRAINT ck_bat_on_demand_status CHECK (request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED', 'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED'))
);
CREATE INDEX ix_bat_on_demand_status ON bat_on_demand_request (request_status, requested_at);
CREATE INDEX ix_bat_on_demand_transaction ON bat_on_demand_request (transaction_id);
CREATE INDEX ix_bat_on_demand_spring ON bat_on_demand_request (spring_batch_execution_id);
COMMENT ON TABLE bat_on_demand_request IS 'BAT 온디맨드 배치 온라인 접수';
COMMENT ON COLUMN bat_on_demand_request.execution_request_id IS '온라인 접수 실행 요청 ID';
COMMENT ON COLUMN bat_on_demand_request.standard_batch_id IS 'B 유형 10자리 표준 배치 ID';
COMMENT ON COLUMN bat_on_demand_request.idempotency_key IS '중복 접수 방지 멱등 키';
COMMENT ON COLUMN bat_on_demand_request.transaction_id IS '온라인 접수 거래 ID';
COMMENT ON COLUMN bat_on_demand_request.business_date IS '배치 업무 기준일 YYYYMMDD';
COMMENT ON COLUMN bat_on_demand_request.request_status IS 'REQUESTED, RUNNING, COMPLETED, FAILED, RESTARTED, STOPPING 등 접수 상태';
COMMENT ON COLUMN bat_on_demand_request.parameters_json IS '검증된 배치 업무 파라미터 JSON';
COMMENT ON COLUMN bat_on_demand_request.request_reason IS '실행 감사 사유';
COMMENT ON COLUMN bat_on_demand_request.request_user IS '실행 요청자';
COMMENT ON COLUMN bat_on_demand_request.cpf_execution_id IS 'BAT 배치 실행 메타 ID';
COMMENT ON COLUMN bat_on_demand_request.spring_batch_execution_id IS 'Spring Batch JobExecution ID';
COMMENT ON COLUMN bat_on_demand_request.result_json IS '마스킹된 실행 결과 JSON';
COMMENT ON COLUMN bat_on_demand_request.failure_code IS '실패 코드';
COMMENT ON COLUMN bat_on_demand_request.failure_message IS '민감정보가 제거된 실패 메시지';
COMMENT ON COLUMN bat_on_demand_request.requested_at IS '접수일시';
COMMENT ON COLUMN bat_on_demand_request.completed_at IS '완료일시';
COMMENT ON COLUMN bat_on_demand_request.created_by IS '등록자';
COMMENT ON COLUMN bat_on_demand_request.created_at IS '등록일시';
COMMENT ON COLUMN bat_on_demand_request.updated_by IS '수정자';
COMMENT ON COLUMN bat_on_demand_request.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_on_demand_request BEFORE UPDATE ON bat_on_demand_request FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_operation_log (
    operation_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    execution_id NUMBER(19),
    operation_type VARCHAR2(30 CHAR) NOT NULL,
    operator_id VARCHAR2(100 CHAR) NOT NULL,
    reason VARCHAR2(500 CHAR) NOT NULL,
    before_data CLOB,
    after_data CLOB,
    result_type CHAR(1 CHAR) NOT NULL DEFAULT 'S',
    result_message VARCHAR2(1000 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_operation_log PRIMARY KEY (operation_id)
);
CREATE INDEX ix_bat_operation_job_time ON bat_operation_log (job_id, created_at);
CREATE INDEX ix_bat_operation_execution ON bat_operation_log (execution_id);
COMMENT ON TABLE bat_operation_log IS 'BAT 배치 운영 작업 로그';
COMMENT ON COLUMN bat_operation_log.operation_id IS '배치 운영 로그 순번';
COMMENT ON COLUMN bat_operation_log.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_operation_log.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_operation_log.operation_type IS '운영 작업 유형';
COMMENT ON COLUMN bat_operation_log.operator_id IS '운영자 ID';
COMMENT ON COLUMN bat_operation_log.reason IS '운영 사유';
COMMENT ON COLUMN bat_operation_log.before_data IS '작업 전 데이터';
COMMENT ON COLUMN bat_operation_log.after_data IS '작업 후 데이터';
COMMENT ON COLUMN bat_operation_log.result_type IS '결과 유형';
COMMENT ON COLUMN bat_operation_log.result_message IS '결과 메시지';
COMMENT ON COLUMN bat_operation_log.created_by IS '등록자';
COMMENT ON COLUMN bat_operation_log.created_at IS '등록일시';
COMMENT ON COLUMN bat_operation_log.updated_by IS '수정자';
COMMENT ON COLUMN bat_operation_log.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_operation_log BEFORE UPDATE ON bat_operation_log FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_operation_log_archive (
    operation_id NUMBER(19) NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    execution_id NUMBER(19),
    operation_type VARCHAR2(30 CHAR) NOT NULL,
    operator_id VARCHAR2(100 CHAR) NOT NULL,
    reason VARCHAR2(500 CHAR) NOT NULL,
    before_data CLOB,
    after_data CLOB,
    result_type CHAR(1 CHAR) NOT NULL DEFAULT 'S',
    result_message VARCHAR2(1000 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    archived_by VARCHAR2(100 CHAR) NOT NULL,
    archive_reason VARCHAR2(500 CHAR) NOT NULL,
    CONSTRAINT pk_bat_operation_log_archive PRIMARY KEY (operation_id)
);
CREATE INDEX ix_bat_operation_archive_job_time ON bat_operation_log_archive (job_id, created_at);
CREATE INDEX ix_bat_operation_archive_archived ON bat_operation_log_archive (archived_at);
COMMENT ON TABLE bat_operation_log_archive IS 'BAT 운영 로그 보관소';
COMMENT ON COLUMN bat_operation_log_archive.operation_id IS '원본 배치 운영 로그 순번';
COMMENT ON COLUMN bat_operation_log_archive.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_operation_log_archive.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_operation_log_archive.operation_type IS '운영 작업 유형';
COMMENT ON COLUMN bat_operation_log_archive.operator_id IS '운영자 ID';
COMMENT ON COLUMN bat_operation_log_archive.reason IS '운영 사유';
COMMENT ON COLUMN bat_operation_log_archive.before_data IS '작업 전 데이터';
COMMENT ON COLUMN bat_operation_log_archive.after_data IS '작업 후 데이터';
COMMENT ON COLUMN bat_operation_log_archive.result_type IS '결과 유형';
COMMENT ON COLUMN bat_operation_log_archive.result_message IS '결과 메시지';
COMMENT ON COLUMN bat_operation_log_archive.created_by IS '원본 등록자';
COMMENT ON COLUMN bat_operation_log_archive.created_at IS '원본 등록일시';
COMMENT ON COLUMN bat_operation_log_archive.updated_by IS '원본 수정자';
COMMENT ON COLUMN bat_operation_log_archive.updated_at IS '원본 수정일시';
COMMENT ON COLUMN bat_operation_log_archive.archived_at IS '보관 일시';
COMMENT ON COLUMN bat_operation_log_archive.archived_by IS '보관 수행자';
COMMENT ON COLUMN bat_operation_log_archive.archive_reason IS '보관 사유';

CREATE TABLE bat_remote_message_ledger (
    direction_cd VARCHAR2(20 CHAR) NOT NULL,
    message_id VARCHAR2(64 CHAR) NOT NULL,
    payload_sha256 VARCHAR2(64 CHAR) NOT NULL,
    status_cd VARCHAR2(20 CHAR) NOT NULL,
    owner_id VARCHAR2(150 CHAR) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    attempt_no NUMBER(10) NOT NULL DEFAULT 1,
    last_error_cd VARCHAR2(100 CHAR) DEFAULT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version_no NUMBER(19) NOT NULL DEFAULT 1,
    CONSTRAINT pk_bat_remote_message_ledger PRIMARY KEY (direction_cd, message_id),
    CONSTRAINT ck_bat_remote_msg_attempt CHECK (attempt_no > 0),
    CONSTRAINT ck_bat_remote_msg_version CHECK (version_no > 0)
);
CREATE INDEX idx_bat_remote_msg_status ON bat_remote_message_ledger (status_cd, lease_until);
CREATE INDEX idx_bat_remote_msg_expiry ON bat_remote_message_ledger (expires_at);
COMMENT ON TABLE bat_remote_message_ledger IS 'Kafka Remote Batch at-least-once Idempotency/Fencing Ledger';
COMMENT ON COLUMN bat_remote_message_ledger.direction_cd IS 'REQUEST 또는 REPLY';
COMMENT ON COLUMN bat_remote_message_ledger.message_id IS '안정 Remote Message 식별자';
COMMENT ON COLUMN bat_remote_message_ledger.payload_sha256 IS 'Payload SHA-256';
COMMENT ON COLUMN bat_remote_message_ledger.status_cd IS 'PROCESSING COMPLETE FAILED';
COMMENT ON COLUMN bat_remote_message_ledger.owner_id IS '현재 처리 인스턴스';
COMMENT ON COLUMN bat_remote_message_ledger.lease_until IS '처리 Lease 만료 일시';
COMMENT ON COLUMN bat_remote_message_ledger.expires_at IS 'Message TTL 만료 일시';
COMMENT ON COLUMN bat_remote_message_ledger.attempt_no IS '처리 시도 횟수';
COMMENT ON COLUMN bat_remote_message_ledger.last_error_cd IS '마지막 Sanitized 오류 코드';
COMMENT ON COLUMN bat_remote_message_ledger.created_at IS '최초 수신 일시';
COMMENT ON COLUMN bat_remote_message_ledger.updated_at IS '최종 상태 변경 일시';
COMMENT ON COLUMN bat_remote_message_ledger.version_no IS 'Fencing/낙관적 잠금 버전';

CREATE TABLE bat_runtime_command (
    command_id VARCHAR2(80 CHAR),
    idempotency_key VARCHAR2(160 CHAR) NOT NULL,
    command_type VARCHAR2(80 CHAR) NOT NULL,
    target_type VARCHAR2(40 CHAR) NOT NULL,
    target_snapshot CLOB,
    target_snapshot_hash VARCHAR2(128 CHAR),
    expected_version NUMBER(19),
    requested_by VARCHAR2(120 CHAR) NOT NULL,
    reason_text VARCHAR2(1000 CHAR) NOT NULL,
    approval_policy_version VARCHAR2(80 CHAR),
    approval_request_id VARCHAR2(80 CHAR),
    approved_by VARCHAR2(120 CHAR),
    command_state VARCHAR2(40 CHAR) NOT NULL,
    execution_attempt NUMBER(10) NOT NULL DEFAULT 0,
    result_text CLOB,
    failure_stage VARCHAR2(80 CHAR),
    before_state CLOB,
    after_state CLOB,
    result_code VARCHAR2(80 CHAR),
    requested_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    transaction_id CHAR(34 CHAR),
    evidence_ref VARCHAR2(500 CHAR),
    CONSTRAINT pk_bat_runtime_command PRIMARY KEY (command_id),
    CONSTRAINT idempotency_key UNIQUE (idempotency_key)
);
COMMENT ON TABLE bat_runtime_command IS 'BAT approved runtime command';
COMMENT ON COLUMN bat_runtime_command.command_id IS 'Runtime command identifier';
COMMENT ON COLUMN bat_runtime_command.idempotency_key IS 'Command idempotency key';
COMMENT ON COLUMN bat_runtime_command.command_type IS 'Approved command type';
COMMENT ON COLUMN bat_runtime_command.target_type IS 'Command target type';
COMMENT ON COLUMN bat_runtime_command.target_snapshot IS 'Command target snapshot JSON';
COMMENT ON COLUMN bat_runtime_command.target_snapshot_hash IS 'Target snapshot checksum';
COMMENT ON COLUMN bat_runtime_command.expected_version IS 'Expected target version';
COMMENT ON COLUMN bat_runtime_command.requested_by IS 'Command requester';
COMMENT ON COLUMN bat_runtime_command.reason_text IS 'Mandatory command reason';
COMMENT ON COLUMN bat_runtime_command.approval_policy_version IS 'Approval policy version';
COMMENT ON COLUMN bat_runtime_command.approval_request_id IS 'ADM approval request identifier';
COMMENT ON COLUMN bat_runtime_command.approved_by IS 'Command approver';
COMMENT ON COLUMN bat_runtime_command.command_state IS 'Command lifecycle state';
COMMENT ON COLUMN bat_runtime_command.execution_attempt IS 'Execution attempt count';
COMMENT ON COLUMN bat_runtime_command.result_text IS 'Command result detail';
COMMENT ON COLUMN bat_runtime_command.failure_stage IS 'Last failed stage';
COMMENT ON COLUMN bat_runtime_command.before_state IS 'State before operation';
COMMENT ON COLUMN bat_runtime_command.after_state IS 'State after operation';
COMMENT ON COLUMN bat_runtime_command.result_code IS 'Command result code';
COMMENT ON COLUMN bat_runtime_command.requested_at IS 'Command request time';
COMMENT ON COLUMN bat_runtime_command.expires_at IS 'Command expiry time';
COMMENT ON COLUMN bat_runtime_command.updated_at IS 'Last command state update time';
COMMENT ON COLUMN bat_runtime_command.transaction_id IS 'CPF transactionId';
COMMENT ON COLUMN bat_runtime_command.evidence_ref IS 'Audit evidence reference';
CREATE OR REPLACE TRIGGER trg_touch_bat_runtime_command BEFORE UPDATE ON bat_runtime_command FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_runtime_instance (
    instance_id VARCHAR2(160 CHAR),
    runtime_role VARCHAR2(40 CHAR) NOT NULL,
    service_id VARCHAR2(120 CHAR) NOT NULL,
    was_id VARCHAR2(120 CHAR),
    host_alias VARCHAR2(160 CHAR),
    zone_id VARCHAR2(80 CHAR),
    pool_id VARCHAR2(80 CHAR),
    artifact_version VARCHAR2(80 CHAR) NOT NULL,
    git_sha VARCHAR2(64 CHAR),
    artifact_checksum VARCHAR2(128 CHAR),
    profile_name VARCHAR2(80 CHAR),
    desired_state VARCHAR2(32 CHAR) NOT NULL DEFAULT 'RUNNING',
    actual_state VARCHAR2(32 CHAR) NOT NULL DEFAULT 'UNKNOWN',
    config_version VARCHAR2(80 CHAR),
    schema_compatibility VARCHAR2(120 CHAR),
    started_at TIMESTAMP(6),
    last_heartbeat_at TIMESTAMP(6),
    fencing_token NUMBER(19) NOT NULL DEFAULT 0,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_runtime_instance PRIMARY KEY (instance_id)
);
CREATE INDEX ix_bat_runtime_instance_service ON bat_runtime_instance (service_id, actual_state);
CREATE INDEX ix_bat_runtime_instance_heartbeat ON bat_runtime_instance (last_heartbeat_at);
COMMENT ON TABLE bat_runtime_instance IS 'BAT standalone runtime instance registry';
COMMENT ON COLUMN bat_runtime_instance.instance_id IS 'Runtime instance identifier';
COMMENT ON COLUMN bat_runtime_instance.runtime_role IS 'Standalone runtime role';
COMMENT ON COLUMN bat_runtime_instance.service_id IS 'Runtime service identifier';
COMMENT ON COLUMN bat_runtime_instance.was_id IS 'WAS identifier';
COMMENT ON COLUMN bat_runtime_instance.host_alias IS 'Registered host alias';
COMMENT ON COLUMN bat_runtime_instance.zone_id IS 'Availability zone identifier';
COMMENT ON COLUMN bat_runtime_instance.pool_id IS 'Runtime pool identifier';
COMMENT ON COLUMN bat_runtime_instance.artifact_version IS 'Running artifact version';
COMMENT ON COLUMN bat_runtime_instance.git_sha IS 'Running source commit SHA';
COMMENT ON COLUMN bat_runtime_instance.artifact_checksum IS 'Running artifact checksum';
COMMENT ON COLUMN bat_runtime_instance.profile_name IS 'Active runtime profile';
COMMENT ON COLUMN bat_runtime_instance.desired_state IS 'Control-plane desired state';
COMMENT ON COLUMN bat_runtime_instance.actual_state IS 'Last observed runtime state';
COMMENT ON COLUMN bat_runtime_instance.config_version IS 'Applied configuration version';
COMMENT ON COLUMN bat_runtime_instance.schema_compatibility IS 'Supported schema version range';
COMMENT ON COLUMN bat_runtime_instance.started_at IS 'Runtime start time';
COMMENT ON COLUMN bat_runtime_instance.last_heartbeat_at IS 'Last heartbeat time';
COMMENT ON COLUMN bat_runtime_instance.fencing_token IS 'Monotonic instance fencing token';
COMMENT ON COLUMN bat_runtime_instance.row_version IS 'Optimistic locking version';
COMMENT ON COLUMN bat_runtime_instance.created_at IS 'Registration time';
COMMENT ON COLUMN bat_runtime_instance.updated_at IS 'Last state update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_runtime_instance BEFORE UPDATE ON bat_runtime_instance FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_scheduler_lease (
    scheduler_key VARCHAR2(100 CHAR),
    owner_instance_id VARCHAR2(160 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    last_heartbeat_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_scheduler_lease PRIMARY KEY (scheduler_key)
);
CREATE INDEX ix_bat_scheduler_lease_expire ON bat_scheduler_lease (lease_until);
COMMENT ON TABLE bat_scheduler_lease IS 'BAT scheduler leader lease';
COMMENT ON COLUMN bat_scheduler_lease.scheduler_key IS 'Scheduler leadership key';
COMMENT ON COLUMN bat_scheduler_lease.owner_instance_id IS 'Current leader instance identifier';
COMMENT ON COLUMN bat_scheduler_lease.fencing_token IS 'Monotonic leadership fencing token';
COMMENT ON COLUMN bat_scheduler_lease.lease_until IS 'Leadership lease expiry time';
COMMENT ON COLUMN bat_scheduler_lease.last_heartbeat_at IS 'Leader heartbeat time';
COMMENT ON COLUMN bat_scheduler_lease.updated_at IS 'Last lease update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_scheduler_lease BEFORE UPDATE ON bat_scheduler_lease FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_version_compatibility (
    compatibility_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    environment_id VARCHAR2(80 CHAR) NOT NULL DEFAULT '*',
    provider_coordinate VARCHAR2(200 CHAR) NOT NULL,
    consumer_coordinate VARCHAR2(200 CHAR) NOT NULL DEFAULT '*',
    min_version VARCHAR2(80 CHAR),
    max_version VARCHAR2(80 CHAR),
    schema_range VARCHAR2(120 CHAR),
    required_capability VARCHAR2(80 CHAR),
    enabled_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    CONSTRAINT pk_bat_version_compatibility PRIMARY KEY (compatibility_id)
);
COMMENT ON TABLE bat_version_compatibility IS 'BAT artifact and schema compatibility contract';
COMMENT ON COLUMN bat_version_compatibility.compatibility_id IS 'Compatibility rule identifier';
COMMENT ON COLUMN bat_version_compatibility.environment_id IS 'Applicable environment identifier';
COMMENT ON COLUMN bat_version_compatibility.provider_coordinate IS 'Provider artifact coordinate';
COMMENT ON COLUMN bat_version_compatibility.consumer_coordinate IS 'Consumer artifact coordinate';
COMMENT ON COLUMN bat_version_compatibility.min_version IS 'Minimum compatible version';
COMMENT ON COLUMN bat_version_compatibility.max_version IS 'Maximum compatible version';
COMMENT ON COLUMN bat_version_compatibility.schema_range IS 'Compatible schema version range';
COMMENT ON COLUMN bat_version_compatibility.required_capability IS 'Required runtime capability';
COMMENT ON COLUMN bat_version_compatibility.enabled_yn IS 'Rule enabled flag';

CREATE TABLE bat_worker (
    worker_id VARCHAR2(160 CHAR) NOT NULL,
    instance_id VARCHAR2(160 CHAR) NOT NULL,
    host_name VARCHAR2(150 CHAR),
    process_id VARCHAR2(80 CHAR),
    thread_name VARCHAR2(160 CHAR),
    worker_version VARCHAR2(80 CHAR) NOT NULL DEFAULT 'unknown',
    capabilities_json CLOB,
    max_concurrency NUMBER(10) NOT NULL DEFAULT 1,
    queue_capacity NUMBER(10) NOT NULL DEFAULT 1,
    control_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'RUNNING',
    worker_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'IDLE',
    active_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    last_heartbeat_at TIMESTAMP(3),
    current_job_id VARCHAR2(100 CHAR),
    current_execution_id NUMBER(19),
    description VARCHAR2(500 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_worker PRIMARY KEY (worker_id)
);
CREATE INDEX ix_bat_worker_instance ON bat_worker (instance_id, active_yn);
CREATE INDEX ix_bat_worker_status ON bat_worker (worker_status, last_heartbeat_at);
CREATE INDEX ix_bat_worker_control ON bat_worker (control_status, active_yn, last_heartbeat_at);
CREATE INDEX ix_bat_worker_current_job ON bat_worker (current_job_id, current_execution_id);
COMMENT ON TABLE bat_worker IS 'BAT 배치 worker heartbeat';
COMMENT ON COLUMN bat_worker.worker_id IS '배치 worker ID';
COMMENT ON COLUMN bat_worker.instance_id IS '서버 인스턴스 ID';
COMMENT ON COLUMN bat_worker.host_name IS '호스트명';
COMMENT ON COLUMN bat_worker.process_id IS '프로세스 ID';
COMMENT ON COLUMN bat_worker.thread_name IS '스레드명';
COMMENT ON COLUMN bat_worker.worker_version IS 'worker 배포 버전';
COMMENT ON COLUMN bat_worker.capabilities_json IS 'worker 지원 Job 및 capability JSON';
COMMENT ON COLUMN bat_worker.max_concurrency IS 'worker 최대 동시 실행 수';
COMMENT ON COLUMN bat_worker.queue_capacity IS 'worker 내부 대기열 허용 수';
COMMENT ON COLUMN bat_worker.control_status IS 'RUNNING, DRAINING, STOPPED 제어 상태';
COMMENT ON COLUMN bat_worker.worker_status IS 'worker 상태';
COMMENT ON COLUMN bat_worker.active_yn IS '활성 여부';
COMMENT ON COLUMN bat_worker.last_heartbeat_at IS '마지막 heartbeat 일시';
COMMENT ON COLUMN bat_worker.current_job_id IS '현재 실행 Job ID';
COMMENT ON COLUMN bat_worker.current_execution_id IS '현재 BAT 배치 실행 순번';
COMMENT ON COLUMN bat_worker.description IS 'worker 설명';
COMMENT ON COLUMN bat_worker.created_by IS '등록자';
COMMENT ON COLUMN bat_worker.created_at IS '등록일시';
COMMENT ON COLUMN bat_worker.updated_by IS '수정자';
COMMENT ON COLUMN bat_worker.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_worker BEFORE UPDATE ON bat_worker FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID NUMBER(19) NOT NULL,
    VERSION NUMBER(19),
    JOB_NAME VARCHAR2(100 CHAR) NOT NULL,
    JOB_KEY VARCHAR2(32 CHAR) NOT NULL,
    CONSTRAINT pk_BATCH_JOB_INSTANCE PRIMARY KEY (JOB_INSTANCE_ID),
    CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);
COMMENT ON TABLE BATCH_JOB_INSTANCE IS 'Spring Batch 표준 JobInstance 저장소';
COMMENT ON COLUMN BATCH_JOB_INSTANCE.JOB_INSTANCE_ID IS 'Spring Batch JobInstance 순번';
COMMENT ON COLUMN BATCH_JOB_INSTANCE.VERSION IS '낙관적 잠금 버전';
COMMENT ON COLUMN BATCH_JOB_INSTANCE.JOB_NAME IS 'Spring Batch Job 이름';
COMMENT ON COLUMN BATCH_JOB_INSTANCE.JOB_KEY IS 'Job 파라미터 식별 키';

CREATE TABLE cpf_batch_approved_launch (
    approval_id VARCHAR2(120 CHAR) NOT NULL,
    job_id VARCHAR2(80 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    definition_checksum CHAR(64 CHAR) NOT NULL,
    approval_status VARCHAR2(20 CHAR) NOT NULL,
    launch_request_json CLOB NOT NULL,
    effective_from TIMESTAMP(6) NOT NULL,
    effective_until TIMESTAMP(6),
    approved_by VARCHAR2(120 CHAR) NOT NULL,
    approved_at TIMESTAMP(6) NOT NULL,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    CONSTRAINT pk_cpf_batch_approved_launch PRIMARY KEY (approval_id),
    CONSTRAINT uk_cpf_bat_approved_def UNIQUE (job_id, definition_version, definition_checksum),
    CONSTRAINT ck_cpf_bat_approval_status CHECK (approval_status IN ('APPROVED', 'REVOKED', 'EXPIRED')),
    CONSTRAINT ck_cpf_bat_approval_version CHECK (row_version >= 0)
);
COMMENT ON TABLE cpf_batch_approved_launch IS '사전 승인된 불변 Spring Batch Launch Request';
COMMENT ON COLUMN cpf_batch_approved_launch.approval_id IS '승인된 Launch Request 식별자';
COMMENT ON COLUMN cpf_batch_approved_launch.job_id IS '승인 대상 Batch Job ID';
COMMENT ON COLUMN cpf_batch_approved_launch.definition_version IS '승인 대상 불변 정의 Version';
COMMENT ON COLUMN cpf_batch_approved_launch.definition_checksum IS '승인 대상 정의 SHA-256';
COMMENT ON COLUMN cpf_batch_approved_launch.approval_status IS '승인 Lifecycle 상태';
COMMENT ON COLUMN cpf_batch_approved_launch.launch_request_json IS '서명·검증된 불변 Launch Request JSON';
COMMENT ON COLUMN cpf_batch_approved_launch.effective_from IS '승인 효력 시작 시각';
COMMENT ON COLUMN cpf_batch_approved_launch.effective_until IS '승인 효력 종료 시각';
COMMENT ON COLUMN cpf_batch_approved_launch.approved_by IS '승인자 식별자';
COMMENT ON COLUMN cpf_batch_approved_launch.approved_at IS '승인 시각';
COMMENT ON COLUMN cpf_batch_approved_launch.row_version IS '승인 상태 낙관적 잠금 Version';

CREATE TABLE cpf_batch_execution_control (
    cpf_execution_id VARCHAR2(80 CHAR) NOT NULL,
    job_id VARCHAR2(80 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    approval_id VARCHAR2(120 CHAR) NOT NULL,
    operator_id VARCHAR2(120 CHAR) NOT NULL,
    reason VARCHAR2(500 CHAR) NOT NULL,
    idempotency_key VARCHAR2(200 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    job_instance_id NUMBER(19),
    job_execution_id NUMBER(19),
    control_status VARCHAR2(40 CHAR) NOT NULL,
    unknown_reason VARCHAR2(100 CHAR),
    unknown_detail VARCHAR2(4000 CHAR),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    idempotency_scope VARCHAR2(400 CHAR) NOT NULL,
    request_hash CHAR(64 CHAR) NOT NULL,
    plan_checksum CHAR(64 CHAR) NOT NULL,
    control_version NUMBER(19) NOT NULL DEFAULT 1,
    reconcile_attempts NUMBER(10) NOT NULL DEFAULT 0,
    reconcile_after TIMESTAMP(6),
    last_error_code VARCHAR2(100 CHAR),
    last_error_detail VARCHAR2(4000 CHAR),
    CONSTRAINT pk_cpf_batch_execution_control PRIMARY KEY (cpf_execution_id),
    CONSTRAINT uk_cpf_bat_exec_idem_scope UNIQUE (idempotency_scope, idempotency_key),
    CONSTRAINT ck_cpf_bat_fencing_pos CHECK (fencing_token > 0),
    CONSTRAINT ck_cpf_bat_request_hash CHECK (REGEXP_LIKE(request_hash, '^[0-9a-f]{64}$')),
    CONSTRAINT ck_cpf_bat_plan_hash CHECK (REGEXP_LIKE(plan_checksum, '^[0-9a-f]{64}$')),
    CONSTRAINT ck_cpf_bat_control_version CHECK (control_version > 0),
    CONSTRAINT ck_cpf_bat_reconcile_attempt CHECK (reconcile_attempts >= 0),
    CONSTRAINT ck_cpf_bat_control_status CHECK (control_status IN ('RESERVED', 'STARTING', 'STARTED', 'STOPPING', 'STOPPED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT', 'ABANDONING', 'ABANDONED', 'REJECTED'))
);
CREATE INDEX ix_cpf_bat_exec_job ON cpf_batch_execution_control (job_id, definition_version, created_at);
CREATE INDEX ix_cpf_bat_exec_sb ON cpf_batch_execution_control (job_execution_id);
CREATE INDEX ix_cpf_bat_exec_reconcile ON cpf_batch_execution_control (control_status, reconcile_after, updated_at);
COMMENT ON TABLE cpf_batch_execution_control IS 'CPF 승인·멱등·Fencing 기반 Spring Batch 실행 Control Ledger';
COMMENT ON COLUMN cpf_batch_execution_control.cpf_execution_id IS 'CPF Batch 실행 식별자';
COMMENT ON COLUMN cpf_batch_execution_control.job_id IS 'Batch Job ID';
COMMENT ON COLUMN cpf_batch_execution_control.definition_version IS '실행에 고정된 정의 Version';
COMMENT ON COLUMN cpf_batch_execution_control.approval_id IS '실행 승인 식별자';
COMMENT ON COLUMN cpf_batch_execution_control.operator_id IS '실행 요청 운영자';
COMMENT ON COLUMN cpf_batch_execution_control.reason IS '승인된 실행 사유';
COMMENT ON COLUMN cpf_batch_execution_control.idempotency_key IS 'Scope 내부 실행 멱등 Key';
COMMENT ON COLUMN cpf_batch_execution_control.fencing_token IS 'Control Plane Fencing Token';
COMMENT ON COLUMN cpf_batch_execution_control.job_instance_id IS 'Spring Batch JobInstance ID';
COMMENT ON COLUMN cpf_batch_execution_control.job_execution_id IS 'Spring Batch JobExecution ID';
COMMENT ON COLUMN cpf_batch_execution_control.control_status IS 'CPF 실행 Control 상태';
COMMENT ON COLUMN cpf_batch_execution_control.unknown_reason IS 'UNKNOWN_RESULT 판정 사유 Code';
COMMENT ON COLUMN cpf_batch_execution_control.unknown_detail IS 'Masking된 UNKNOWN_RESULT 상세';
COMMENT ON COLUMN cpf_batch_execution_control.created_at IS '실행 예약 생성 시각';
COMMENT ON COLUMN cpf_batch_execution_control.updated_at IS '마지막 Control 상태 변경 시각';
COMMENT ON COLUMN cpf_batch_execution_control.idempotency_scope IS '실행 멱등성 격리 Scope';
COMMENT ON COLUMN cpf_batch_execution_control.request_hash IS 'Canonical Launch Request SHA-256';
COMMENT ON COLUMN cpf_batch_execution_control.plan_checksum IS '검증된 실행 Plan SHA-256';
COMMENT ON COLUMN cpf_batch_execution_control.control_version IS 'Control 상태 CAS Version';
COMMENT ON COLUMN cpf_batch_execution_control.reconcile_attempts IS 'UNKNOWN_RESULT 대사 시도 횟수';
COMMENT ON COLUMN cpf_batch_execution_control.reconcile_after IS '다음 대사 가능 시각';
COMMENT ON COLUMN cpf_batch_execution_control.last_error_code IS '마지막 표준 오류 Code';
COMMENT ON COLUMN cpf_batch_execution_control.last_error_detail IS 'Masking된 마지막 오류 상세';
CREATE OR REPLACE TRIGGER trg_touch_cpf_batch_execution_control BEFORE UPDATE ON cpf_batch_execution_control FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE cpf_batch_execution_epoch (
    job_id VARCHAR2(80 CHAR) NOT NULL,
    current_fencing_token NUMBER(19) NOT NULL,
    epoch_version NUMBER(19) NOT NULL DEFAULT 1,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_cpf_batch_execution_epoch PRIMARY KEY (job_id),
    CONSTRAINT ck_cpf_batch_execution_epoch_token CHECK (current_fencing_token > 0),
    CONSTRAINT ck_cpf_batch_execution_epoch_version CHECK (epoch_version > 0)
);
CREATE INDEX ix_cpf_batch_execution_epoch_updated ON cpf_batch_execution_epoch (updated_at);
COMMENT ON TABLE cpf_batch_execution_epoch IS 'Batch Job별 최신 Fencing Epoch Ledger';
COMMENT ON COLUMN cpf_batch_execution_epoch.job_id IS 'Batch Job ID별 최신 Fencing Epoch 식별자';
COMMENT ON COLUMN cpf_batch_execution_epoch.current_fencing_token IS '현재 유효한 최신 Fencing Token';
COMMENT ON COLUMN cpf_batch_execution_epoch.epoch_version IS 'Epoch 낙관적 잠금 버전';
COMMENT ON COLUMN cpf_batch_execution_epoch.updated_at IS '최신 Epoch 변경 시각';
CREATE OR REPLACE TRIGGER trg_touch_cpf_batch_execution_epoch BEFORE UPDATE ON cpf_batch_execution_epoch FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_deployment_execution (
    deployment_id VARCHAR2(80 CHAR) NOT NULL,
    cell_id VARCHAR2(120 CHAR) NOT NULL,
    idempotency_key VARCHAR2(160 CHAR) NOT NULL,
    from_version VARCHAR2(80 CHAR),
    to_version VARCHAR2(80 CHAR) NOT NULL,
    strategy_code VARCHAR2(32 CHAR) NOT NULL,
    execution_state VARCHAR2(40 CHAR) NOT NULL,
    failure_stage VARCHAR2(80 CHAR),
    result_message VARCHAR2(4000 CHAR),
    requested_by VARCHAR2(120 CHAR) NOT NULL,
    approved_by VARCHAR2(120 CHAR) NOT NULL,
    reason_text VARCHAR2(1000 CHAR) NOT NULL,
    started_at TIMESTAMP(6),
    finished_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    idempotency_scope VARCHAR2(120 CHAR) NOT NULL,
    request_hash CHAR(64 CHAR) NOT NULL,
    expected_version NUMBER(19),
    approval_request_id VARCHAR2(120 CHAR),
    reconcile_requested_by VARCHAR2(120 CHAR),
    reconcile_approved_by VARCHAR2(120 CHAR),
    reconcile_approval_request_id VARCHAR2(120 CHAR),
    reconcile_reason VARCHAR2(1000 CHAR),
    reconciled_at TIMESTAMP(6),
    CONSTRAINT pk_bat_deployment_execution PRIMARY KEY (deployment_id),
    CONSTRAINT uk_bat_deploy_exec_scope_idem UNIQUE (idempotency_scope, idempotency_key),
    CONSTRAINT ck_bat_deploy_exec_request_hash CHECK (REGEXP_LIKE(request_hash, '^[0-9a-f]{64}$')),
    CONSTRAINT fk_bat_deployment_execution_cell FOREIGN KEY (cell_id) REFERENCES bat_deployment_cell (cell_id)
);
CREATE INDEX ix_bat_deployment_execution_cell_state ON bat_deployment_execution (cell_id, execution_state);
CREATE INDEX ix_bat_deploy_exec_request_hash ON bat_deployment_execution (request_hash);
CREATE INDEX ix_bat_deploy_exec_reconciled ON bat_deployment_execution (execution_state, reconciled_at);
COMMENT ON TABLE bat_deployment_execution IS 'BAT approved deployment execution';
COMMENT ON COLUMN bat_deployment_execution.deployment_id IS 'Deployment execution identifier';
COMMENT ON COLUMN bat_deployment_execution.cell_id IS 'Target deployment cell identifier';
COMMENT ON COLUMN bat_deployment_execution.idempotency_key IS 'Deployment idempotency key';
COMMENT ON COLUMN bat_deployment_execution.from_version IS 'Previous artifact version';
COMMENT ON COLUMN bat_deployment_execution.to_version IS 'Target artifact version';
COMMENT ON COLUMN bat_deployment_execution.strategy_code IS 'ROLLING/CANARY/BLUE_GREEN strategy';
COMMENT ON COLUMN bat_deployment_execution.execution_state IS 'Deployment execution state';
COMMENT ON COLUMN bat_deployment_execution.failure_stage IS 'Failed deployment stage';
COMMENT ON COLUMN bat_deployment_execution.result_message IS 'Deployment result detail';
COMMENT ON COLUMN bat_deployment_execution.requested_by IS 'Deployment requester';
COMMENT ON COLUMN bat_deployment_execution.approved_by IS 'Deployment approver';
COMMENT ON COLUMN bat_deployment_execution.reason_text IS 'Mandatory deployment reason';
COMMENT ON COLUMN bat_deployment_execution.started_at IS 'Deployment start time';
COMMENT ON COLUMN bat_deployment_execution.finished_at IS 'Deployment finish time';
COMMENT ON COLUMN bat_deployment_execution.created_at IS 'Deployment record time';
COMMENT ON COLUMN bat_deployment_execution.idempotency_scope IS 'Cell-scoped deployment idempotency scope';
COMMENT ON COLUMN bat_deployment_execution.request_hash IS 'Canonical approved deployment request SHA-256';
COMMENT ON COLUMN bat_deployment_execution.expected_version IS 'Expected deployment plan version';
COMMENT ON COLUMN bat_deployment_execution.approval_request_id IS 'Approval request identifier';
COMMENT ON COLUMN bat_deployment_execution.reconcile_requested_by IS 'Reconciliation requester';
COMMENT ON COLUMN bat_deployment_execution.reconcile_approved_by IS 'Reconciliation approver';
COMMENT ON COLUMN bat_deployment_execution.reconcile_approval_request_id IS 'Reconciliation approval request identifier';
COMMENT ON COLUMN bat_deployment_execution.reconcile_reason IS 'Mandatory reconciliation reason';
COMMENT ON COLUMN bat_deployment_execution.reconciled_at IS 'Reconciliation completion time';

CREATE TABLE bat_deployment_instance (
    cell_id VARCHAR2(120 CHAR) NOT NULL,
    instance_id VARCHAR2(160 CHAR) NOT NULL,
    host_alias VARCHAR2(160 CHAR) NOT NULL,
    port_no NUMBER(10) NOT NULL,
    profile_name VARCHAR2(80 CHAR) NOT NULL,
    zone_id VARCHAR2(80 CHAR),
    pool_id VARCHAR2(80 CHAR),
    agent_base_url VARCHAR2(500 CHAR) NOT NULL,
    config_ref VARCHAR2(1000 CHAR),
    desired_state VARCHAR2(32 CHAR) NOT NULL,
    CONSTRAINT pk_bat_deployment_instance PRIMARY KEY (cell_id, instance_id),
    CONSTRAINT uk_bat_deployment_instance_id UNIQUE (instance_id),
    CONSTRAINT fk_bat_deployment_instance_cell FOREIGN KEY (cell_id) REFERENCES bat_deployment_cell (cell_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_deployment_instance IS 'BAT deployment cell instance projection';
COMMENT ON COLUMN bat_deployment_instance.cell_id IS 'Deployment cell identifier';
COMMENT ON COLUMN bat_deployment_instance.instance_id IS 'Runtime instance identifier';
COMMENT ON COLUMN bat_deployment_instance.host_alias IS 'Target host alias';
COMMENT ON COLUMN bat_deployment_instance.port_no IS 'Runtime service port';
COMMENT ON COLUMN bat_deployment_instance.profile_name IS 'Runtime profile name';
COMMENT ON COLUMN bat_deployment_instance.zone_id IS 'Availability zone identifier';
COMMENT ON COLUMN bat_deployment_instance.pool_id IS 'Runtime pool identifier';
COMMENT ON COLUMN bat_deployment_instance.agent_base_url IS 'Approved host-agent base URL';
COMMENT ON COLUMN bat_deployment_instance.config_ref IS 'External configuration reference';
COMMENT ON COLUMN bat_deployment_instance.desired_state IS 'Desired instance state';

CREATE TABLE bat_job (
    job_id VARCHAR2(100 CHAR) NOT NULL,
    job_name VARCHAR2(150 CHAR) NOT NULL,
    job_type VARCHAR2(30 CHAR) NOT NULL DEFAULT 'TASKLET',
    description VARCHAR2(500 CHAR),
    restartable_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    use_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_definition_version NUMBER(19),
    published_definition_checksum VARCHAR2(128 CHAR),
    executor_reference VARCHAR2(300 CHAR),
    definition_published_at TIMESTAMP(3),
    CONSTRAINT pk_bat_job PRIMARY KEY (job_id),
    CONSTRAINT fk_bat_job_published_definition FOREIGN KEY (job_id, published_definition_version) REFERENCES bat_job_definition_version (job_id, definition_version)
);
CREATE INDEX ix_bat_job_use ON bat_job (use_yn, job_type);
COMMENT ON TABLE bat_job IS 'BAT 배치 Job 기준';
COMMENT ON COLUMN bat_job.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_job.job_name IS '배치 Job 이름';
COMMENT ON COLUMN bat_job.job_type IS '배치 Job 유형';
COMMENT ON COLUMN bat_job.description IS '배치 설명';
COMMENT ON COLUMN bat_job.restartable_yn IS '재시작 가능 여부';
COMMENT ON COLUMN bat_job.use_yn IS '사용 여부';
COMMENT ON COLUMN bat_job.created_by IS '등록자';
COMMENT ON COLUMN bat_job.created_at IS '등록일시';
COMMENT ON COLUMN bat_job.updated_by IS '수정자';
COMMENT ON COLUMN bat_job.updated_at IS '수정일시';
COMMENT ON COLUMN bat_job.published_definition_version IS '현재 Runtime에 고정 반영된 Job Definition Version';
COMMENT ON COLUMN bat_job.published_definition_checksum IS 'Published Definition 무결성 Checksum';
COMMENT ON COLUMN bat_job.executor_reference IS '검증된 Executor Catalog Reference';
COMMENT ON COLUMN bat_job.definition_published_at IS 'Published Definition Runtime 반영 시각';
CREATE OR REPLACE TRIGGER trg_touch_bat_job BEFORE UPDATE ON bat_job FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_job_dependency (
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    related_job_id VARCHAR2(80 CHAR) NOT NULL,
    condition_code VARCHAR2(40 CHAR) NOT NULL,
    timeout_seconds NUMBER(19) NOT NULL DEFAULT 0,
    required_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    sort_order NUMBER(10) NOT NULL DEFAULT 0,
    CONSTRAINT pk_bat_job_dependency PRIMARY KEY (job_id, definition_version, related_job_id),
    CONSTRAINT ck_bat_job_dep_self CHECK (job_id <> related_job_id),
    CONSTRAINT fk_bat_job_dep_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE
);
COMMENT ON TABLE bat_job_dependency IS 'BAT Versioned Job Dependency';
COMMENT ON COLUMN bat_job_dependency.job_id IS 'Job ID';
COMMENT ON COLUMN bat_job_dependency.definition_version IS 'Definition Version';
COMMENT ON COLUMN bat_job_dependency.related_job_id IS '선행 Job';
COMMENT ON COLUMN bat_job_dependency.condition_code IS '의존 조건';
COMMENT ON COLUMN bat_job_dependency.timeout_seconds IS '대기 Timeout';
COMMENT ON COLUMN bat_job_dependency.required_yn IS '필수 여부';
COMMENT ON COLUMN bat_job_dependency.sort_order IS '정렬';

CREATE TABLE bat_job_pack_job (
    job_pack_id VARCHAR2(120 CHAR) NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    restartable_yn CHAR(1 CHAR) NOT NULL,
    center_cut_provider_key VARCHAR2(100 CHAR),
    center_cut_handler_key VARCHAR2(100 CHAR),
    CONSTRAINT pk_bat_job_pack_job PRIMARY KEY (job_pack_id, job_id),
    CONSTRAINT fk_bat_job_pack_job_pack FOREIGN KEY (job_pack_id) REFERENCES bat_job_pack (job_pack_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_job_pack_job IS 'BAT job-pack job projection';
COMMENT ON COLUMN bat_job_pack_job.job_pack_id IS 'Owning job-pack identifier';
COMMENT ON COLUMN bat_job_pack_job.job_id IS 'Published job identifier';
COMMENT ON COLUMN bat_job_pack_job.restartable_yn IS 'Job restartability flag';
COMMENT ON COLUMN bat_job_pack_job.center_cut_provider_key IS 'Center-cut target provider key';
COMMENT ON COLUMN bat_job_pack_job.center_cut_handler_key IS 'Center-cut item handler key';

CREATE TABLE bat_job_parameter_definition (
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    parameter_name VARCHAR2(100 CHAR) NOT NULL,
    parameter_type VARCHAR2(40 CHAR) NOT NULL,
    label_text VARCHAR2(200 CHAR),
    description_text VARCHAR2(1000 CHAR),
    required_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    sensitive_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    default_value VARCHAR2(1000 CHAR),
    allowed_values CLOB,
    validation_pattern VARCHAR2(1000 CHAR),
    min_value DECIMAL(38,10),
    max_value DECIMAL(38,10),
    min_length NUMBER(10),
    max_length NUMBER(10),
    reference_type VARCHAR2(80 CHAR),
    alias_required_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    runtime_override_allowed_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    sort_order NUMBER(10) NOT NULL DEFAULT 0,
    CONSTRAINT pk_bat_job_parameter_definition PRIMARY KEY (job_id, definition_version, parameter_name),
    CONSTRAINT fk_bat_job_param_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE
);
COMMENT ON TABLE bat_job_parameter_definition IS 'BAT Typed Parameter Schema';
COMMENT ON COLUMN bat_job_parameter_definition.job_id IS 'Job ID';
COMMENT ON COLUMN bat_job_parameter_definition.definition_version IS 'Definition Version';
COMMENT ON COLUMN bat_job_parameter_definition.parameter_name IS 'Parameter 이름';
COMMENT ON COLUMN bat_job_parameter_definition.parameter_type IS 'Parameter 유형';
COMMENT ON COLUMN bat_job_parameter_definition.label_text IS 'UI Label';
COMMENT ON COLUMN bat_job_parameter_definition.description_text IS '설명';
COMMENT ON COLUMN bat_job_parameter_definition.required_yn IS '필수 여부';
COMMENT ON COLUMN bat_job_parameter_definition.sensitive_yn IS '민감정보 여부';
COMMENT ON COLUMN bat_job_parameter_definition.default_value IS '기본값';
COMMENT ON COLUMN bat_job_parameter_definition.allowed_values IS '허용값';
COMMENT ON COLUMN bat_job_parameter_definition.validation_pattern IS '검증 Pattern';
COMMENT ON COLUMN bat_job_parameter_definition.min_value IS '최솟값';
COMMENT ON COLUMN bat_job_parameter_definition.max_value IS '최댓값';
COMMENT ON COLUMN bat_job_parameter_definition.min_length IS '최소 길이';
COMMENT ON COLUMN bat_job_parameter_definition.max_length IS '최대 길이';
COMMENT ON COLUMN bat_job_parameter_definition.reference_type IS '참조 유형';
COMMENT ON COLUMN bat_job_parameter_definition.alias_required_yn IS 'Alias 강제';
COMMENT ON COLUMN bat_job_parameter_definition.runtime_override_allowed_yn IS '실행 Override';
COMMENT ON COLUMN bat_job_parameter_definition.sort_order IS '정렬';

CREATE TABLE bat_job_runtime_projection (
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    definition_checksum VARCHAR2(64 CHAR) NOT NULL,
    projection_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'ACTIVE',
    executor_type VARCHAR2(40 CHAR) NOT NULL,
    executor_reference VARCHAR2(300 CHAR) NOT NULL,
    trigger_type VARCHAR2(30 CHAR) NOT NULL,
    trigger_expression VARCHAR2(500 CHAR),
    timezone_id VARCHAR2(100 CHAR) NOT NULL,
    projection_json CLOB NOT NULL,
    projection_hash VARCHAR2(64 CHAR) NOT NULL,
    effective_from TIMESTAMP,
    effective_until TIMESTAMP,
    published_by VARCHAR2(100 CHAR) NOT NULL,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retired_at TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 1,
    CONSTRAINT pk_bat_job_runtime_projection PRIMARY KEY (job_id, definition_version),
    CONSTRAINT fk_bat_job_projection_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version)
);
CREATE INDEX ix_bat_job_projection_status ON bat_job_runtime_projection (projection_status, effective_from, effective_until);
CREATE UNIQUE INDEX ix_bat_job_projection_hash ON bat_job_runtime_projection (projection_hash);
COMMENT ON TABLE bat_job_runtime_projection IS 'Published Batch Definition Runtime 정본';
COMMENT ON COLUMN bat_job_runtime_projection.job_id IS 'Job ID';
COMMENT ON COLUMN bat_job_runtime_projection.definition_version IS 'Published Definition Version';
COMMENT ON COLUMN bat_job_runtime_projection.definition_checksum IS 'Definition Checksum';
COMMENT ON COLUMN bat_job_runtime_projection.projection_status IS 'Projection 상태';
COMMENT ON COLUMN bat_job_runtime_projection.executor_type IS 'Executor 유형';
COMMENT ON COLUMN bat_job_runtime_projection.executor_reference IS 'Executor Reference';
COMMENT ON COLUMN bat_job_runtime_projection.trigger_type IS 'Trigger 유형';
COMMENT ON COLUMN bat_job_runtime_projection.trigger_expression IS 'Trigger 표현식';
COMMENT ON COLUMN bat_job_runtime_projection.timezone_id IS 'Timezone';
COMMENT ON COLUMN bat_job_runtime_projection.projection_json IS '불변 Runtime Projection JSON';
COMMENT ON COLUMN bat_job_runtime_projection.projection_hash IS 'Projection SHA-256';
COMMENT ON COLUMN bat_job_runtime_projection.effective_from IS '유효 시작';
COMMENT ON COLUMN bat_job_runtime_projection.effective_until IS '유효 종료';
COMMENT ON COLUMN bat_job_runtime_projection.published_by IS 'Publish 운영자';
COMMENT ON COLUMN bat_job_runtime_projection.published_at IS 'Publish 시각';
COMMENT ON COLUMN bat_job_runtime_projection.retired_at IS 'Retire 시각';
COMMENT ON COLUMN bat_job_runtime_projection.row_version IS '낙관적 버전';

CREATE TABLE bat_job_runtime_projection_outbox (
    outbox_id VARCHAR2(100 CHAR) NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    event_type VARCHAR2(40 CHAR) NOT NULL,
    payload_hash VARCHAR2(64 CHAR) NOT NULL,
    event_payload CLOB NOT NULL,
    delivery_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'PENDING',
    lease_owner VARCHAR2(100 CHAR),
    lease_until TIMESTAMP,
    fencing_token NUMBER(19) NOT NULL DEFAULT 0,
    attempt_count NUMBER(10) NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error_code VARCHAR2(100 CHAR),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    CONSTRAINT pk_bat_job_runtime_projection_outbox PRIMARY KEY (outbox_id),
    CONSTRAINT fk_bat_projection_outbox_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version)
);
CREATE INDEX ix_bat_projection_outbox_claim ON bat_job_runtime_projection_outbox (delivery_status, next_attempt_at, lease_until);
CREATE INDEX ix_bat_projection_outbox_job ON bat_job_runtime_projection_outbox (job_id, definition_version, created_at);
COMMENT ON TABLE bat_job_runtime_projection_outbox IS 'Batch Runtime Projection Durable Outbox';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.outbox_id IS 'Outbox ID';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.job_id IS 'Job ID';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.definition_version IS 'Definition Version';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.event_type IS 'PUBLISH/RETIRE';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.payload_hash IS 'Payload Hash';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.event_payload IS 'Event Payload';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.delivery_status IS 'Delivery 상태';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.lease_owner IS 'Lease Owner';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.lease_until IS 'Lease 만료';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.fencing_token IS 'Fencing Token';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.attempt_count IS '시도 횟수';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.next_attempt_at IS '다음 시도';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.last_error_code IS '마지막 오류 코드';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.created_at IS '생성 시각';
COMMENT ON COLUMN bat_job_runtime_projection_outbox.delivered_at IS '전달 시각';

CREATE TABLE bat_runtime_capability (
    instance_id VARCHAR2(160 CHAR) NOT NULL,
    capability_code VARCHAR2(80 CHAR) NOT NULL,
    CONSTRAINT pk_bat_runtime_capability PRIMARY KEY (instance_id, capability_code),
    CONSTRAINT fk_bat_runtime_capability_instance FOREIGN KEY (instance_id) REFERENCES bat_runtime_instance (instance_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_runtime_capability IS 'BAT runtime capability projection';
COMMENT ON COLUMN bat_runtime_capability.instance_id IS 'Runtime instance identifier';
COMMENT ON COLUMN bat_runtime_capability.capability_code IS 'Advertised capability code';

CREATE TABLE bat_runtime_command_attempt (
    attempt_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    command_id VARCHAR2(80 CHAR) NOT NULL,
    attempt_no NUMBER(10) NOT NULL,
    instance_id VARCHAR2(160 CHAR),
    stage_code VARCHAR2(80 CHAR) NOT NULL,
    attempt_state VARCHAR2(40 CHAR) NOT NULL,
    result_message VARCHAR2(4000 CHAR),
    started_at TIMESTAMP(6) NOT NULL,
    finished_at TIMESTAMP(6),
    CONSTRAINT pk_bat_runtime_command_attempt PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_runtime_command_attempt UNIQUE (command_id, attempt_no, instance_id, stage_code),
    CONSTRAINT fk_bat_runtime_command_attempt_command FOREIGN KEY (command_id) REFERENCES bat_runtime_command (command_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_runtime_command_attempt_instance ON bat_runtime_command_attempt (instance_id, started_at);
COMMENT ON TABLE bat_runtime_command_attempt IS 'BAT runtime command execution attempt';
COMMENT ON COLUMN bat_runtime_command_attempt.attempt_id IS 'Command attempt identifier';
COMMENT ON COLUMN bat_runtime_command_attempt.command_id IS 'Runtime command identifier';
COMMENT ON COLUMN bat_runtime_command_attempt.attempt_no IS 'Command attempt number';
COMMENT ON COLUMN bat_runtime_command_attempt.instance_id IS 'Target runtime instance identifier';
COMMENT ON COLUMN bat_runtime_command_attempt.stage_code IS 'Attempt execution stage';
COMMENT ON COLUMN bat_runtime_command_attempt.attempt_state IS 'Attempt result state';
COMMENT ON COLUMN bat_runtime_command_attempt.result_message IS 'Attempt result detail';
COMMENT ON COLUMN bat_runtime_command_attempt.started_at IS 'Attempt start time';
COMMENT ON COLUMN bat_runtime_command_attempt.finished_at IS 'Attempt finish time';

CREATE TABLE bat_runtime_heartbeat (
    heartbeat_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    instance_id VARCHAR2(160 CHAR) NOT NULL,
    heartbeat_at TIMESTAMP(6) NOT NULL,
    ready_yn CHAR(1 CHAR) NOT NULL,
    available_capacity NUMBER(10) NOT NULL DEFAULT 0,
    queue_depth NUMBER(19) NOT NULL DEFAULT 0,
    draining_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    current_execution_count NUMBER(10) NOT NULL DEFAULT 0,
    active_lease_count NUMBER(10) NOT NULL DEFAULT 0,
    last_error_code VARCHAR2(80 CHAR),
    deployment_version VARCHAR2(80 CHAR),
    CONSTRAINT pk_bat_runtime_heartbeat PRIMARY KEY (heartbeat_id),
    CONSTRAINT fk_bat_runtime_heartbeat_instance FOREIGN KEY (instance_id) REFERENCES bat_runtime_instance (instance_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_runtime_heartbeat_instance ON bat_runtime_heartbeat (instance_id, heartbeat_at);
COMMENT ON TABLE bat_runtime_heartbeat IS 'BAT runtime heartbeat event';
COMMENT ON COLUMN bat_runtime_heartbeat.heartbeat_id IS 'Heartbeat event identifier';
COMMENT ON COLUMN bat_runtime_heartbeat.instance_id IS 'Runtime instance identifier';
COMMENT ON COLUMN bat_runtime_heartbeat.heartbeat_at IS 'Heartbeat observation time';
COMMENT ON COLUMN bat_runtime_heartbeat.ready_yn IS 'Readiness flag';
COMMENT ON COLUMN bat_runtime_heartbeat.available_capacity IS 'Available execution capacity';
COMMENT ON COLUMN bat_runtime_heartbeat.queue_depth IS 'Observed queue depth';
COMMENT ON COLUMN bat_runtime_heartbeat.draining_yn IS 'Drain mode flag';
COMMENT ON COLUMN bat_runtime_heartbeat.current_execution_count IS 'Current execution count';
COMMENT ON COLUMN bat_runtime_heartbeat.active_lease_count IS 'Active lease count';
COMMENT ON COLUMN bat_runtime_heartbeat.last_error_code IS 'Last runtime error code';
COMMENT ON COLUMN bat_runtime_heartbeat.deployment_version IS 'Observed deployment version';

CREATE TABLE BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    VERSION NUMBER(19),
    JOB_INSTANCE_ID NUMBER(19) NOT NULL,
    CREATE_TIME TIMESTAMP(6) NOT NULL,
    START_TIME TIMESTAMP(6) DEFAULT NULL,
    END_TIME TIMESTAMP(6) DEFAULT NULL,
    STATUS VARCHAR2(10 CHAR),
    EXIT_CODE VARCHAR2(2500 CHAR),
    EXIT_MESSAGE VARCHAR2(2500 CHAR),
    LAST_UPDATED TIMESTAMP(6),
    CONSTRAINT pk_BATCH_JOB_EXECUTION PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);
COMMENT ON TABLE BATCH_JOB_EXECUTION IS 'Spring Batch 표준 JobExecution 저장소';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.JOB_EXECUTION_ID IS 'Spring Batch JobExecution 순번';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.VERSION IS '낙관적 잠금 버전';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.JOB_INSTANCE_ID IS 'Spring Batch JobInstance 순번';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.CREATE_TIME IS '실행 생성 일시';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.START_TIME IS '실행 시작 일시';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.END_TIME IS '실행 종료 일시';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.STATUS IS '실행 상태';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.EXIT_CODE IS '종료 코드';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.EXIT_MESSAGE IS '종료 메시지';
COMMENT ON COLUMN BATCH_JOB_EXECUTION.LAST_UPDATED IS '마지막 수정 일시';

CREATE TABLE cpf_batch_execution_link (
    cpf_execution_id VARCHAR2(80 CHAR) NOT NULL,
    link_key VARCHAR2(80 CHAR) NOT NULL,
    job_id VARCHAR2(80 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    spring_job_instance_id NUMBER(19) NOT NULL,
    spring_job_execution_id NUMBER(19) NOT NULL,
    spring_step_execution_id NUMBER(19),
    spring_status VARCHAR2(40 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_cpf_batch_execution_link PRIMARY KEY (cpf_execution_id, link_key),
    CONSTRAINT ck_cpf_bat_link_fencing CHECK (fencing_token > 0),
    CONSTRAINT fk_cpf_bat_exec_link FOREIGN KEY (cpf_execution_id) REFERENCES cpf_batch_execution_control (cpf_execution_id) ON DELETE CASCADE
);
CREATE INDEX ix_cpf_bat_link_sb ON cpf_batch_execution_link (spring_job_execution_id, spring_step_execution_id);
COMMENT ON TABLE cpf_batch_execution_link IS 'CPF 실행과 Spring Batch Job/Step Metadata 연결 Projection';
COMMENT ON COLUMN cpf_batch_execution_link.cpf_execution_id IS 'CPF Batch 실행 식별자';
COMMENT ON COLUMN cpf_batch_execution_link.link_key IS 'Job/Step 실행 Link 식별 Key';
COMMENT ON COLUMN cpf_batch_execution_link.job_id IS 'Batch Job ID';
COMMENT ON COLUMN cpf_batch_execution_link.definition_version IS '실행에 고정된 정의 Version';
COMMENT ON COLUMN cpf_batch_execution_link.spring_job_instance_id IS 'Spring Batch JobInstance ID';
COMMENT ON COLUMN cpf_batch_execution_link.spring_job_execution_id IS 'Spring Batch JobExecution ID';
COMMENT ON COLUMN cpf_batch_execution_link.spring_step_execution_id IS 'Spring Batch StepExecution ID';
COMMENT ON COLUMN cpf_batch_execution_link.spring_status IS 'Spring Batch 실행 상태';
COMMENT ON COLUMN cpf_batch_execution_link.fencing_token IS 'Link 생성 시 검증된 Fencing Token';
COMMENT ON COLUMN cpf_batch_execution_link.created_at IS 'Link 생성 시각';
COMMENT ON COLUMN cpf_batch_execution_link.updated_at IS 'Link 마지막 동기화 시각';
CREATE OR REPLACE TRIGGER trg_touch_cpf_batch_execution_link BEFORE UPDATE ON cpf_batch_execution_link FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_center_cut_job (
    center_cut_job_id VARCHAR2(100 CHAR) NOT NULL,
    batch_job_id VARCHAR2(100 CHAR),
    center_cut_job_name VARCHAR2(150 CHAR) NOT NULL,
    provider_key VARCHAR2(100 CHAR) NOT NULL,
    handler_key VARCHAR2(100 CHAR) NOT NULL,
    chunk_size NUMBER(10) NOT NULL DEFAULT 100,
    retry_limit NUMBER(10) NOT NULL DEFAULT 3,
    use_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    description VARCHAR2(500 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_center_cut_job PRIMARY KEY (center_cut_job_id),
    CONSTRAINT fk_bat_center_cut_job_batch FOREIGN KEY (batch_job_id) REFERENCES bat_job (job_id) ON DELETE SET NULL
);
CREATE INDEX ix_bat_center_cut_job_batch ON bat_center_cut_job (batch_job_id, use_yn);
COMMENT ON TABLE bat_center_cut_job IS 'BAT 센터컷 Job 정의';
COMMENT ON COLUMN bat_center_cut_job.center_cut_job_id IS '센터컷 Job ID';
COMMENT ON COLUMN bat_center_cut_job.batch_job_id IS '연결된 BAT 배치 Job ID';
COMMENT ON COLUMN bat_center_cut_job.center_cut_job_name IS '센터컷 Job 명';
COMMENT ON COLUMN bat_center_cut_job.provider_key IS '대상 조회 Provider 식별자';
COMMENT ON COLUMN bat_center_cut_job.handler_key IS '처리 Handler 식별자';
COMMENT ON COLUMN bat_center_cut_job.chunk_size IS '한 번에 조회할 대상 건수';
COMMENT ON COLUMN bat_center_cut_job.retry_limit IS '최대 재처리 횟수';
COMMENT ON COLUMN bat_center_cut_job.use_yn IS '사용 여부';
COMMENT ON COLUMN bat_center_cut_job.description IS '센터컷 Job 설명';
COMMENT ON COLUMN bat_center_cut_job.created_by IS '등록자';
COMMENT ON COLUMN bat_center_cut_job.created_at IS '등록일시';
COMMENT ON COLUMN bat_center_cut_job.updated_by IS '수정자';
COMMENT ON COLUMN bat_center_cut_job.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_job BEFORE UPDATE ON bat_center_cut_job FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_deployment_instance_result (
    deployment_result_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    deployment_id VARCHAR2(80 CHAR) NOT NULL,
    sequence_no NUMBER(10) NOT NULL,
    instance_id VARCHAR2(160 CHAR) NOT NULL,
    stage_code VARCHAR2(80 CHAR) NOT NULL,
    result_state VARCHAR2(40 CHAR) NOT NULL,
    result_message VARCHAR2(4000 CHAR),
    recorded_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_bat_deployment_instance_result PRIMARY KEY (deployment_result_id),
    CONSTRAINT uk_bat_deployment_instance_result UNIQUE (deployment_id, sequence_no),
    CONSTRAINT fk_bat_deployment_instance_result_execution FOREIGN KEY (deployment_id) REFERENCES bat_deployment_execution (deployment_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_deployment_instance_result_instance ON bat_deployment_instance_result (instance_id, recorded_at);
COMMENT ON TABLE bat_deployment_instance_result IS 'BAT per-instance deployment result';
COMMENT ON COLUMN bat_deployment_instance_result.deployment_result_id IS 'Instance result identifier';
COMMENT ON COLUMN bat_deployment_instance_result.deployment_id IS 'Deployment execution identifier';
COMMENT ON COLUMN bat_deployment_instance_result.sequence_no IS 'Ordered result sequence';
COMMENT ON COLUMN bat_deployment_instance_result.instance_id IS 'Target runtime instance identifier';
COMMENT ON COLUMN bat_deployment_instance_result.stage_code IS 'Deployment stage code';
COMMENT ON COLUMN bat_deployment_instance_result.result_state IS 'Instance stage result state';
COMMENT ON COLUMN bat_deployment_instance_result.result_message IS 'Instance stage result detail';
COMMENT ON COLUMN bat_deployment_instance_result.recorded_at IS 'Result record time';

CREATE TABLE bat_execution (
    execution_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    schedule_id VARCHAR2(100 CHAR),
    job_parameters VARCHAR2(2000 CHAR),
    execution_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'READY',
    spring_batch_execution_id NUMBER(19),
    spring_batch_job_instance_id NUMBER(19),
    business_date DATE,
    run_id VARCHAR2(120 CHAR),
    rerun_id VARCHAR2(120 CHAR),
    original_job_execution_id NUMBER(19),
    restart_attempt NUMBER(10) NOT NULL DEFAULT 0,
    batch_instance_id VARCHAR2(100 CHAR),
    instance_id VARCHAR2(160 CHAR),
    worker_id VARCHAR2(160 CHAR),
    required_worker_version VARCHAR2(80 CHAR),
    required_capability VARCHAR2(120 CHAR),
    transaction_id CHAR(34 CHAR),
    transaction_segment_id VARCHAR2(120 CHAR),
    parent_segment_id VARCHAR2(120 CHAR),
    job_log_relative_path VARCHAR2(1000 CHAR),
    start_time TIMESTAMP(3),
    end_time TIMESTAMP(3),
    read_count NUMBER(19) NOT NULL DEFAULT 0,
    write_count NUMBER(19) NOT NULL DEFAULT 0,
    skip_count NUMBER(19) NOT NULL DEFAULT 0,
    total_count NUMBER(19) NOT NULL DEFAULT 0,
    processed_count NUMBER(19) NOT NULL DEFAULT 0,
    success_count NUMBER(19) NOT NULL DEFAULT 0,
    failure_count NUMBER(19) NOT NULL DEFAULT 0,
    retry_count NUMBER(19) NOT NULL DEFAULT 0,
    stop_requested_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    avg_elapsed_ms NUMBER(19) NOT NULL DEFAULT 0,
    max_elapsed_ms NUMBER(19) NOT NULL DEFAULT 0,
    last_heartbeat_at TIMESTAMP(3),
    current_step_name VARCHAR2(150 CHAR),
    error_message CLOB,
    requested_by VARCHAR2(100 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    definition_version NUMBER(19),
    definition_checksum VARCHAR2(128 CHAR),
    CONSTRAINT pk_bat_execution PRIMARY KEY (execution_id),
    CONSTRAINT fk_bat_execution_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_execution_instance FOREIGN KEY (batch_instance_id) REFERENCES bat_instance (instance_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version)
);
CREATE INDEX ix_bat_execution_job_time ON bat_execution (job_id, start_time);
CREATE INDEX ix_bat_execution_status ON bat_execution (execution_status, start_time);
CREATE INDEX ix_bat_execution_spring ON bat_execution (spring_batch_execution_id);
CREATE INDEX ix_bat_execution_job_instance ON bat_execution (spring_batch_job_instance_id, business_date);
CREATE INDEX ix_bat_execution_worker ON bat_execution (worker_id, execution_status, start_time);
CREATE INDEX ix_bat_execution_instance ON bat_execution (instance_id, start_time);
CREATE INDEX ix_bat_execution_claim ON bat_execution (execution_status, required_worker_version, required_capability, execution_id);
CREATE INDEX ix_bat_execution_transaction ON bat_execution (transaction_id);
CREATE INDEX ix_bat_execution_segment ON bat_execution (transaction_segment_id, parent_segment_id);
CREATE INDEX ix_bat_execution_heartbeat ON bat_execution (execution_status, last_heartbeat_at);
COMMENT ON TABLE bat_execution IS 'BAT 배치 실행 이력';
COMMENT ON COLUMN bat_execution.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_execution.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_execution.schedule_id IS '배치 스케줄 ID';
COMMENT ON COLUMN bat_execution.job_parameters IS '배치 파라미터';
COMMENT ON COLUMN bat_execution.execution_status IS '실행 상태';
COMMENT ON COLUMN bat_execution.spring_batch_execution_id IS 'Spring Batch JobExecution ID';
COMMENT ON COLUMN bat_execution.spring_batch_job_instance_id IS 'Spring Batch JobInstance ID';
COMMENT ON COLUMN bat_execution.business_date IS 'JobInstance 시작 시 확정한 업무일자';
COMMENT ON COLUMN bat_execution.run_id IS '최초 실행 회차 ID';
COMMENT ON COLUMN bat_execution.rerun_id IS '운영 재수행 ID';
COMMENT ON COLUMN bat_execution.original_job_execution_id IS '재시작 기준 원 JobExecution ID';
COMMENT ON COLUMN bat_execution.restart_attempt IS '동일 JobInstance 재시작 회차';
COMMENT ON COLUMN bat_execution.batch_instance_id IS '배치 인스턴스 ID';
COMMENT ON COLUMN bat_execution.instance_id IS '실행 서버 인스턴스 ID';
COMMENT ON COLUMN bat_execution.worker_id IS '실행 worker ID';
COMMENT ON COLUMN bat_execution.required_worker_version IS '실행에 필요한 worker 버전';
COMMENT ON COLUMN bat_execution.required_capability IS '실행에 필요한 worker capability';
COMMENT ON COLUMN bat_execution.transaction_id IS '전역 거래 ID';
COMMENT ON COLUMN bat_execution.transaction_segment_id IS '배치 Job 거래 구간 ID';
COMMENT ON COLUMN bat_execution.parent_segment_id IS '상위 거래 구간 ID';
COMMENT ON COLUMN bat_execution.job_log_relative_path IS 'CPF_LOG_ROOT 기준 JobInstance 로그 상대 경로';
COMMENT ON COLUMN bat_execution.start_time IS '시작 일시';
COMMENT ON COLUMN bat_execution.end_time IS '종료 일시';
COMMENT ON COLUMN bat_execution.read_count IS '읽은 건수';
COMMENT ON COLUMN bat_execution.write_count IS '처리 건수';
COMMENT ON COLUMN bat_execution.skip_count IS '건너뛴 건수';
COMMENT ON COLUMN bat_execution.total_count IS '전체 처리 대상 건수';
COMMENT ON COLUMN bat_execution.processed_count IS '처리 완료 건수';
COMMENT ON COLUMN bat_execution.success_count IS '성공 처리 건수';
COMMENT ON COLUMN bat_execution.failure_count IS '실패 처리 건수';
COMMENT ON COLUMN bat_execution.retry_count IS '재시도 또는 rollback 건수';
COMMENT ON COLUMN bat_execution.stop_requested_yn IS '운영 중지 요청 여부';
COMMENT ON COLUMN bat_execution.progress_rate IS '진행률';
COMMENT ON COLUMN bat_execution.tps IS '초당 처리 건수';
COMMENT ON COLUMN bat_execution.avg_elapsed_ms IS '평균 처리 시간 밀리초';
COMMENT ON COLUMN bat_execution.max_elapsed_ms IS '최대 처리 시간 밀리초';
COMMENT ON COLUMN bat_execution.last_heartbeat_at IS '실행 메타 마지막 heartbeat 일시';
COMMENT ON COLUMN bat_execution.current_step_name IS '현재 실행 중인 Step 이름';
COMMENT ON COLUMN bat_execution.error_message IS '오류 메시지';
COMMENT ON COLUMN bat_execution.requested_by IS '실행 요청자';
COMMENT ON COLUMN bat_execution.created_by IS '등록자';
COMMENT ON COLUMN bat_execution.created_at IS '등록일시';
COMMENT ON COLUMN bat_execution.updated_by IS '수정자';
COMMENT ON COLUMN bat_execution.updated_at IS '수정일시';
COMMENT ON COLUMN bat_execution.definition_version IS 'Execution 생성 시 고정된 Job Definition Version';
COMMENT ON COLUMN bat_execution.definition_checksum IS 'Execution 생성 시 고정된 Definition Checksum';
CREATE OR REPLACE TRIGGER trg_touch_bat_execution BEFORE UPDATE ON bat_execution FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_job_relation (
    relation_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    related_job_id VARCHAR2(100 CHAR) NOT NULL,
    relation_type VARCHAR2(30 CHAR) NOT NULL,
    trigger_condition VARCHAR2(50 CHAR) NOT NULL DEFAULT 'COMPLETED',
    required_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'COMPLETED',
    sort_order NUMBER(10) NOT NULL DEFAULT 0,
    use_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_job_relation PRIMARY KEY (relation_id),
    CONSTRAINT uk_bat_job_relation UNIQUE (job_id, related_job_id, relation_type),
    CONSTRAINT fk_bat_job_relation_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_job_relation_related FOREIGN KEY (related_job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_job_relation_job ON bat_job_relation (job_id, relation_type, use_yn);
CREATE INDEX ix_bat_job_relation_related ON bat_job_relation (related_job_id, relation_type);
COMMENT ON TABLE bat_job_relation IS 'BAT 배치 선행/후행/트리거 관계';
COMMENT ON COLUMN bat_job_relation.relation_id IS '배치 관계 순번';
COMMENT ON COLUMN bat_job_relation.job_id IS '기준 배치 Job ID';
COMMENT ON COLUMN bat_job_relation.related_job_id IS '연관 배치 Job ID';
COMMENT ON COLUMN bat_job_relation.relation_type IS '관계 유형';
COMMENT ON COLUMN bat_job_relation.trigger_condition IS '트리거 조건';
COMMENT ON COLUMN bat_job_relation.required_status IS '필수 선행 상태';
COMMENT ON COLUMN bat_job_relation.sort_order IS '관계 표시 순서';
COMMENT ON COLUMN bat_job_relation.use_yn IS '사용 여부';
COMMENT ON COLUMN bat_job_relation.created_by IS '등록자';
COMMENT ON COLUMN bat_job_relation.created_at IS '등록일시';
COMMENT ON COLUMN bat_job_relation.updated_by IS '수정자';
COMMENT ON COLUMN bat_job_relation.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_job_relation BEFORE UPDATE ON bat_job_relation FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_schedule (
    schedule_id VARCHAR2(100 CHAR) NOT NULL,
    job_id VARCHAR2(100 CHAR) NOT NULL,
    cron_expression VARCHAR2(100 CHAR) NOT NULL,
    calendar_id VARCHAR2(50 CHAR) NOT NULL DEFAULT 'DEFAULT',
    business_day_only_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    holiday_policy VARCHAR2(30 CHAR) NOT NULL DEFAULT 'SKIP',
    available_start_time TIME,
    available_end_time TIME,
    run_date_pattern VARCHAR2(80 CHAR),
    timezone VARCHAR2(50 CHAR) NOT NULL DEFAULT 'Asia/Seoul',
    enabled_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    last_fire_at TIMESTAMP,
    next_fire_at TIMESTAMP,
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 0,
    definition_version NUMBER(19),
    definition_checksum VARCHAR2(128 CHAR),
    CONSTRAINT pk_bat_schedule PRIMARY KEY (schedule_id),
    CONSTRAINT fk_bat_schedule_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_schedule_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version)
);
CREATE INDEX ix_bat_schedule_job ON bat_schedule (job_id, enabled_yn);
COMMENT ON TABLE bat_schedule IS 'BAT 배치 스케줄';
COMMENT ON COLUMN bat_schedule.schedule_id IS '배치 스케줄 ID';
COMMENT ON COLUMN bat_schedule.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_schedule.cron_expression IS 'Cron 표현식';
COMMENT ON COLUMN bat_schedule.calendar_id IS '적용 영업일 캘린더 ID';
COMMENT ON COLUMN bat_schedule.business_day_only_yn IS '영업일에만 수행 여부';
COMMENT ON COLUMN bat_schedule.holiday_policy IS '휴일 처리 정책';
COMMENT ON COLUMN bat_schedule.available_start_time IS '수행 가능 시작 시각';
COMMENT ON COLUMN bat_schedule.available_end_time IS '수행 가능 종료 시각';
COMMENT ON COLUMN bat_schedule.run_date_pattern IS '수행 일자 패턴';
COMMENT ON COLUMN bat_schedule.timezone IS '스케줄 기준 시간대';
COMMENT ON COLUMN bat_schedule.enabled_yn IS '스케줄 활성 여부';
COMMENT ON COLUMN bat_schedule.last_fire_at IS '마지막 실행 예정 일시';
COMMENT ON COLUMN bat_schedule.next_fire_at IS '다음 실행 예정 일시';
COMMENT ON COLUMN bat_schedule.created_by IS '등록자';
COMMENT ON COLUMN bat_schedule.created_at IS '등록일시';
COMMENT ON COLUMN bat_schedule.updated_by IS '수정자';
COMMENT ON COLUMN bat_schedule.updated_at IS '수정일시';
COMMENT ON COLUMN bat_schedule.definition_version IS 'Schedule이 실행해야 하는 고정 Job Definition Version';
COMMENT ON COLUMN bat_schedule.definition_checksum IS 'Schedule 생성 시 고정된 Definition Checksum';
CREATE OR REPLACE TRIGGER trg_touch_bat_schedule BEFORE UPDATE ON bat_schedule FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    SHORT_CONTEXT VARCHAR2(2500 CHAR) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT pk_BATCH_JOB_EXECUTION_CONTEXT PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);
COMMENT ON TABLE BATCH_JOB_EXECUTION_CONTEXT IS 'Spring Batch 표준 Job 컨텍스트 저장소';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_CONTEXT.JOB_EXECUTION_ID IS 'Spring Batch JobExecution 순번';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_CONTEXT.SHORT_CONTEXT IS '짧은 실행 컨텍스트';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_CONTEXT.SERIALIZED_CONTEXT IS '직렬화 실행 컨텍스트';

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    PARAMETER_NAME VARCHAR2(100 CHAR) NOT NULL,
    PARAMETER_TYPE VARCHAR2(100 CHAR) NOT NULL,
    PARAMETER_VALUE VARCHAR2(2500 CHAR),
    IDENTIFYING CHAR(1 CHAR) NOT NULL,
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);
COMMENT ON TABLE BATCH_JOB_EXECUTION_PARAMS IS 'Spring Batch 표준 Job 파라미터 저장소';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_PARAMS.JOB_EXECUTION_ID IS 'Spring Batch JobExecution 순번';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_PARAMS.PARAMETER_NAME IS '파라미터 이름';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_PARAMS.PARAMETER_TYPE IS '파라미터 Java 유형';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_PARAMS.PARAMETER_VALUE IS '파라미터 값';
COMMENT ON COLUMN BATCH_JOB_EXECUTION_PARAMS.IDENTIFYING IS 'JobInstance 식별 파라미터 여부';

CREATE TABLE BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID NUMBER(19) NOT NULL,
    VERSION NUMBER(19) NOT NULL,
    STEP_NAME VARCHAR2(100 CHAR) NOT NULL,
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    CREATE_TIME TIMESTAMP(6) NOT NULL,
    START_TIME TIMESTAMP(6) DEFAULT NULL,
    END_TIME TIMESTAMP(6) DEFAULT NULL,
    STATUS VARCHAR2(10 CHAR),
    COMMIT_COUNT NUMBER(19),
    READ_COUNT NUMBER(19),
    FILTER_COUNT NUMBER(19),
    WRITE_COUNT NUMBER(19),
    READ_SKIP_COUNT NUMBER(19),
    WRITE_SKIP_COUNT NUMBER(19),
    PROCESS_SKIP_COUNT NUMBER(19),
    ROLLBACK_COUNT NUMBER(19),
    EXIT_CODE VARCHAR2(2500 CHAR),
    EXIT_MESSAGE VARCHAR2(2500 CHAR),
    LAST_UPDATED TIMESTAMP(6),
    CONSTRAINT pk_BATCH_STEP_EXECUTION PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);
COMMENT ON TABLE BATCH_STEP_EXECUTION IS 'Spring Batch 표준 StepExecution 저장소';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.STEP_EXECUTION_ID IS 'Spring Batch StepExecution 순번';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.VERSION IS '낙관적 잠금 버전';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.STEP_NAME IS 'Step 이름';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.JOB_EXECUTION_ID IS 'Spring Batch JobExecution 순번';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.CREATE_TIME IS 'Step 생성 일시';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.START_TIME IS 'Step 시작 일시';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.END_TIME IS 'Step 종료 일시';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.STATUS IS 'Step 상태';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.COMMIT_COUNT IS '커밋 횟수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.READ_COUNT IS '읽은 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.FILTER_COUNT IS '필터 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.WRITE_COUNT IS '쓴 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.READ_SKIP_COUNT IS '읽기 skip 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.WRITE_SKIP_COUNT IS '쓰기 skip 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.PROCESS_SKIP_COUNT IS '처리 skip 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.ROLLBACK_COUNT IS 'rollback 건수';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.EXIT_CODE IS '종료 코드';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.EXIT_MESSAGE IS '종료 메시지';
COMMENT ON COLUMN BATCH_STEP_EXECUTION.LAST_UPDATED IS '마지막 수정 일시';

CREATE TABLE bat_center_cut_execution (
    center_cut_execution_id VARCHAR2(80 CHAR) NOT NULL,
    center_cut_job_id VARCHAR2(100 CHAR) NOT NULL,
    idempotency_key VARCHAR2(160 CHAR) NOT NULL,
    execution_state VARCHAR2(30 CHAR) NOT NULL,
    parameter_ciphertext CLOB NOT NULL,
    parameter_hash VARCHAR2(64 CHAR) NOT NULL,
    parameter_schema_version VARCHAR2(80 CHAR) NOT NULL,
    target_cursor VARCHAR2(1000 CHAR),
    target_complete_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    target_count NUMBER(19) NOT NULL DEFAULT 0,
    tps_limit NUMBER(10) NOT NULL DEFAULT 0,
    concurrency_limit NUMBER(10) NOT NULL DEFAULT 1,
    processed_count NUMBER(19) NOT NULL DEFAULT 0,
    success_count NUMBER(19) NOT NULL DEFAULT 0,
    failure_count NUMBER(19) NOT NULL DEFAULT 0,
    unknown_count NUMBER(19) NOT NULL DEFAULT 0,
    transaction_id CHAR(34 CHAR),
    parent_segment_id VARCHAR2(120 CHAR),
    requested_by VARCHAR2(120 CHAR) NOT NULL,
    reason_text VARCHAR2(1000 CHAR) NOT NULL,
    last_error_message VARCHAR2(1000 CHAR),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    CONSTRAINT pk_bat_center_cut_execution PRIMARY KEY (center_cut_execution_id),
    CONSTRAINT uk_bat_center_cut_execution_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_center_cut_execution_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id)
);
CREATE INDEX ix_bat_center_cut_execution_job_state ON bat_center_cut_execution (center_cut_job_id, execution_state, created_at);
COMMENT ON TABLE bat_center_cut_execution IS 'BAT center-cut immutable execution policy';
COMMENT ON COLUMN bat_center_cut_execution.center_cut_execution_id IS 'Center-cut execution identifier';
COMMENT ON COLUMN bat_center_cut_execution.center_cut_job_id IS 'Center-cut job definition identifier';
COMMENT ON COLUMN bat_center_cut_execution.idempotency_key IS 'Execution idempotency key';
COMMENT ON COLUMN bat_center_cut_execution.execution_state IS 'Center-cut execution state';
COMMENT ON COLUMN bat_center_cut_execution.parameter_ciphertext IS 'Encrypted immutable parameter snapshot';
COMMENT ON COLUMN bat_center_cut_execution.parameter_hash IS 'Parameter snapshot SHA-256';
COMMENT ON COLUMN bat_center_cut_execution.parameter_schema_version IS 'Parameter schema version';
COMMENT ON COLUMN bat_center_cut_execution.target_cursor IS 'Last generated target cursor';
COMMENT ON COLUMN bat_center_cut_execution.target_complete_yn IS 'Target generation completion flag';
COMMENT ON COLUMN bat_center_cut_execution.target_count IS 'Generated target count';
COMMENT ON COLUMN bat_center_cut_execution.tps_limit IS 'Global transactions-per-second limit';
COMMENT ON COLUMN bat_center_cut_execution.concurrency_limit IS 'Global runner concurrency limit';
COMMENT ON COLUMN bat_center_cut_execution.processed_count IS 'Processed item count';
COMMENT ON COLUMN bat_center_cut_execution.success_count IS 'Successful item count';
COMMENT ON COLUMN bat_center_cut_execution.failure_count IS 'Failed item count';
COMMENT ON COLUMN bat_center_cut_execution.unknown_count IS 'Unknown-result item count';
COMMENT ON COLUMN bat_center_cut_execution.transaction_id IS 'CPF transactionId';
COMMENT ON COLUMN bat_center_cut_execution.parent_segment_id IS 'Parent trace segment identifier';
COMMENT ON COLUMN bat_center_cut_execution.requested_by IS 'Execution requester';
COMMENT ON COLUMN bat_center_cut_execution.reason_text IS 'Mandatory execution reason';
COMMENT ON COLUMN bat_center_cut_execution.last_error_message IS 'Last execution error detail';
COMMENT ON COLUMN bat_center_cut_execution.created_at IS 'Execution request time';
COMMENT ON COLUMN bat_center_cut_execution.updated_at IS 'Last execution state update time';
COMMENT ON COLUMN bat_center_cut_execution.completed_at IS 'Execution completion time';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_execution BEFORE UPDATE ON bat_center_cut_execution FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_center_cut_parameter (
    parameter_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    center_cut_job_id VARCHAR2(100 CHAR) NOT NULL,
    parameter_key VARCHAR2(100 CHAR) NOT NULL,
    parameter_value VARCHAR2(1000 CHAR),
    encrypted_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    use_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_center_cut_parameter PRIMARY KEY (parameter_id),
    CONSTRAINT uk_bat_center_cut_parameter UNIQUE (center_cut_job_id, parameter_key),
    CONSTRAINT fk_bat_center_cut_parameter_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_center_cut_parameter IS 'BAT 센터컷 파라미터';
COMMENT ON COLUMN bat_center_cut_parameter.parameter_id IS '센터컷 파라미터 순번';
COMMENT ON COLUMN bat_center_cut_parameter.center_cut_job_id IS '센터컷 Job ID';
COMMENT ON COLUMN bat_center_cut_parameter.parameter_key IS '파라미터 키';
COMMENT ON COLUMN bat_center_cut_parameter.parameter_value IS '파라미터 값';
COMMENT ON COLUMN bat_center_cut_parameter.encrypted_yn IS '암호화 여부';
COMMENT ON COLUMN bat_center_cut_parameter.use_yn IS '사용 여부';
COMMENT ON COLUMN bat_center_cut_parameter.created_by IS '등록자';
COMMENT ON COLUMN bat_center_cut_parameter.created_at IS '등록일시';
COMMENT ON COLUMN bat_center_cut_parameter.updated_by IS '수정자';
COMMENT ON COLUMN bat_center_cut_parameter.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_parameter BEFORE UPDATE ON bat_center_cut_parameter FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_execution_attempt (
    attempt_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    execution_id NUMBER(19) NOT NULL,
    attempt_no NUMBER(10) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    definition_checksum VARCHAR2(128 CHAR) NOT NULL,
    worker_id VARCHAR2(160 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    attempt_status VARCHAR2(40 CHAR) NOT NULL DEFAULT 'RUNNING',
    result_message CLOB,
    executor_type VARCHAR2(40 CHAR),
    exit_code NUMBER(10),
    stdout_text CLOB,
    stderr_text CLOB,
    output_truncated_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    duration_ms NUMBER(19),
    artifact_hash VARCHAR2(128 CHAR),
    unknown_result_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    started_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP(3),
    CONSTRAINT pk_bat_execution_attempt PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_execution_attempt UNIQUE (execution_id, attempt_no),
    CONSTRAINT ck_bat_execution_attempt_status CHECK (attempt_status IN ('RUNNING','COMPLETED','FAILED','TIMEOUT','RETRYABLE_FAILURE','UNKNOWN_RESULT')),
    CONSTRAINT ck_bat_execution_attempt_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT ck_bat_execution_attempt_unknown CHECK (unknown_result_yn IN ('Y','N')),
    CONSTRAINT fk_bat_execution_attempt_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_execution_attempt_status ON bat_execution_attempt (attempt_status, started_at);
CREATE INDEX ix_bat_execution_attempt_worker ON bat_execution_attempt (worker_id, started_at);
COMMENT ON TABLE bat_execution_attempt IS 'BAT 실행별 재시도 및 결과 불명 원장';
COMMENT ON COLUMN bat_execution_attempt.attempt_id IS '배치 실행 시도 식별자';
COMMENT ON COLUMN bat_execution_attempt.execution_id IS '배치 실행 식별자';
COMMENT ON COLUMN bat_execution_attempt.attempt_no IS '1부터 시작하는 실행 시도 번호';
COMMENT ON COLUMN bat_execution_attempt.definition_version IS '시도에 고정된 Definition Version';
COMMENT ON COLUMN bat_execution_attempt.definition_checksum IS '시도에 고정된 Definition Checksum';
COMMENT ON COLUMN bat_execution_attempt.worker_id IS '시도를 소유한 Worker';
COMMENT ON COLUMN bat_execution_attempt.fencing_token IS '시도 소유권 Fencing Token';
COMMENT ON COLUMN bat_execution_attempt.attempt_status IS 'RUNNING/COMPLETED/FAILED/TIMEOUT/RETRYABLE_FAILURE/UNKNOWN_RESULT';
COMMENT ON COLUMN bat_execution_attempt.result_message IS '마스킹된 시도 결과 메시지';
COMMENT ON COLUMN bat_execution_attempt.executor_type IS '실제 실행 Adapter 유형';
COMMENT ON COLUMN bat_execution_attempt.exit_code IS 'Shell/Process 종료 코드';
COMMENT ON COLUMN bat_execution_attempt.stdout_text IS '마스킹된 표준 출력';
COMMENT ON COLUMN bat_execution_attempt.stderr_text IS '마스킹된 표준 오류';
COMMENT ON COLUMN bat_execution_attempt.output_truncated_yn IS '출력 길이 제한 적용 여부';
COMMENT ON COLUMN bat_execution_attempt.duration_ms IS '실행 소요 시간(ms)';
COMMENT ON COLUMN bat_execution_attempt.artifact_hash IS '승인 Script/File Artifact SHA-256';
COMMENT ON COLUMN bat_execution_attempt.unknown_result_yn IS '결과 불명 여부';
COMMENT ON COLUMN bat_execution_attempt.started_at IS '시도 시작 일시';
COMMENT ON COLUMN bat_execution_attempt.finished_at IS '시도 종료 일시';

CREATE TABLE bat_execution_lease (
    lease_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    execution_id NUMBER(19) NOT NULL,
    worker_id VARCHAR2(160 CHAR) NOT NULL,
    lease_token VARCHAR2(80 CHAR) NOT NULL,
    lease_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'CLAIMED',
    claimed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    lease_until TIMESTAMP(3) NOT NULL,
    last_heartbeat_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    attempt_no NUMBER(10) NOT NULL DEFAULT 1,
    takeover_count NUMBER(10) NOT NULL DEFAULT 0,
    fencing_token NUMBER(19) NOT NULL DEFAULT 0,
    released_at TIMESTAMP(3),
    failure_message VARCHAR2(1000 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_execution_lease PRIMARY KEY (lease_id),
    CONSTRAINT uk_bat_execution_lease_execution UNIQUE (execution_id),
    CONSTRAINT uk_bat_execution_lease_token UNIQUE (lease_token),
    CONSTRAINT fk_bat_execution_lease_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_execution_lease_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE RESTRICT
);
CREATE INDEX ix_bat_execution_lease_owner ON bat_execution_lease (worker_id, lease_status, lease_until);
CREATE INDEX ix_bat_execution_lease_expire ON bat_execution_lease (lease_status, lease_until);
COMMENT ON TABLE bat_execution_lease IS 'BAT 배치 worker 실행 claim과 lease';
COMMENT ON COLUMN bat_execution_lease.lease_id IS '배치 실행 lease 순번';
COMMENT ON COLUMN bat_execution_lease.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_execution_lease.worker_id IS '현재 lease 소유 worker ID';
COMMENT ON COLUMN bat_execution_lease.lease_token IS 'lease 갱신·완료 검증 토큰';
COMMENT ON COLUMN bat_execution_lease.lease_status IS 'CLAIMED, RUNNING, RELEASED, EXPIRED 상태';
COMMENT ON COLUMN bat_execution_lease.claimed_at IS '최초 claim 일시';
COMMENT ON COLUMN bat_execution_lease.lease_until IS 'lease 만료 일시';
COMMENT ON COLUMN bat_execution_lease.last_heartbeat_at IS '마지막 lease heartbeat 일시';
COMMENT ON COLUMN bat_execution_lease.attempt_no IS 'claim 시도 회차';
COMMENT ON COLUMN bat_execution_lease.takeover_count IS '만료 후 다른 worker 인수 횟수';
COMMENT ON COLUMN bat_execution_lease.fencing_token IS 'monotonic fencing token';
COMMENT ON COLUMN bat_execution_lease.released_at IS '정상 또는 실패 완료 일시';
COMMENT ON COLUMN bat_execution_lease.failure_message IS '마스킹된 실행 실패 메시지';
COMMENT ON COLUMN bat_execution_lease.created_by IS '등록자';
COMMENT ON COLUMN bat_execution_lease.created_at IS '등록일시';
COMMENT ON COLUMN bat_execution_lease.updated_by IS '수정자';
COMMENT ON COLUMN bat_execution_lease.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_execution_lease BEFORE UPDATE ON bat_execution_lease FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_execution_result_detail (
    execution_id NUMBER(19) NOT NULL,
    definition_version NUMBER(19),
    definition_checksum VARCHAR2(64 CHAR),
    executor_status VARCHAR2(40 CHAR) NOT NULL,
    exit_code NUMBER(10),
    timeout_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    unknown_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    output_truncated_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    output_hash VARCHAR2(64 CHAR),
    artifact_hash VARCHAR2(64 CHAR),
    parameter_snapshot_hash VARCHAR2(64 CHAR),
    result_message VARCHAR2(2000 CHAR),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_execution_result_detail PRIMARY KEY (execution_id),
    CONSTRAINT ck_bat_result_timeout CHECK (timeout_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_unknown CHECK (unknown_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT fk_bat_result_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id)
);
CREATE INDEX ix_bat_result_status ON bat_execution_result_detail (executor_status, created_at);
CREATE INDEX ix_bat_result_unknown ON bat_execution_result_detail (unknown_yn, created_at);
COMMENT ON TABLE bat_execution_result_detail IS 'Batch Executor 상세 결과 원장';
COMMENT ON COLUMN bat_execution_result_detail.execution_id IS 'BAT 실행 ID';
COMMENT ON COLUMN bat_execution_result_detail.definition_version IS 'Definition Version Snapshot';
COMMENT ON COLUMN bat_execution_result_detail.definition_checksum IS 'Definition Checksum Snapshot';
COMMENT ON COLUMN bat_execution_result_detail.executor_status IS 'Executor 상세 상태';
COMMENT ON COLUMN bat_execution_result_detail.exit_code IS 'Process Exit Code';
COMMENT ON COLUMN bat_execution_result_detail.timeout_yn IS 'Timeout 여부';
COMMENT ON COLUMN bat_execution_result_detail.unknown_yn IS '결과 불명 여부';
COMMENT ON COLUMN bat_execution_result_detail.output_truncated_yn IS '출력 절단 여부';
COMMENT ON COLUMN bat_execution_result_detail.output_hash IS '출력 Hash';
COMMENT ON COLUMN bat_execution_result_detail.artifact_hash IS '실행 Artifact Hash';
COMMENT ON COLUMN bat_execution_result_detail.parameter_snapshot_hash IS 'Parameter Snapshot Hash';
COMMENT ON COLUMN bat_execution_result_detail.result_message IS '마스킹 결과';
COMMENT ON COLUMN bat_execution_result_detail.created_at IS '기록 시각';
COMMENT ON COLUMN bat_execution_result_detail.updated_at IS '갱신 시각';
CREATE OR REPLACE TRIGGER trg_touch_bat_execution_result_detail BEFORE UPDATE ON bat_execution_result_detail FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_execution_target (
    target_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    execution_id NUMBER(19),
    job_id VARCHAR2(100 CHAR) NOT NULL,
    schedule_id VARCHAR2(100 CHAR),
    target_instance_id VARCHAR2(100 CHAR),
    business_date DATE,
    planned_run_at TIMESTAMP(3),
    dispatch_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'WAITING',
    dispatch_reason VARCHAR2(500 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_execution_target PRIMARY KEY (target_id),
    CONSTRAINT fk_bat_execution_target_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_execution_target_schedule FOREIGN KEY (schedule_id) REFERENCES bat_schedule (schedule_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_instance FOREIGN KEY (target_instance_id) REFERENCES bat_instance (instance_id) ON DELETE SET NULL
);
CREATE INDEX ix_bat_execution_target_job ON bat_execution_target (job_id, dispatch_status, planned_run_at);
CREATE INDEX ix_bat_execution_target_execution ON bat_execution_target (execution_id);
CREATE INDEX ix_bat_execution_target_instance ON bat_execution_target (target_instance_id, dispatch_status);
COMMENT ON TABLE bat_execution_target IS 'BAT 배치 수행 대상/대기 인스턴스';
COMMENT ON COLUMN bat_execution_target.target_id IS '배치 수행 대상 순번';
COMMENT ON COLUMN bat_execution_target.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_execution_target.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_execution_target.schedule_id IS '배치 스케줄 ID';
COMMENT ON COLUMN bat_execution_target.target_instance_id IS '수행 대상 인스턴스 ID';
COMMENT ON COLUMN bat_execution_target.business_date IS '업무 기준일';
COMMENT ON COLUMN bat_execution_target.planned_run_at IS '예정 수행 일시';
COMMENT ON COLUMN bat_execution_target.dispatch_status IS '배정 상태';
COMMENT ON COLUMN bat_execution_target.dispatch_reason IS '배정 또는 제외 사유';
COMMENT ON COLUMN bat_execution_target.created_by IS '등록자';
COMMENT ON COLUMN bat_execution_target.created_at IS '등록일시';
COMMENT ON COLUMN bat_execution_target.updated_by IS '수정자';
COMMENT ON COLUMN bat_execution_target.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_execution_target BEFORE UPDATE ON bat_execution_target FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_ghost_event (
    ghost_event_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    execution_id NUMBER(19),
    spring_batch_execution_id NUMBER(19),
    job_id VARCHAR2(100 CHAR) NOT NULL,
    instance_id VARCHAR2(160 CHAR),
    worker_id VARCHAR2(160 CHAR),
    ghost_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'DETECTED',
    detected_reason VARCHAR2(1000 CHAR) NOT NULL,
    action_type VARCHAR2(30 CHAR),
    action_reason VARCHAR2(1000 CHAR),
    action_by VARCHAR2(100 CHAR),
    detected_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    action_at TIMESTAMP(3),
    lock_released_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    retryable_yn CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    before_data CLOB,
    after_data CLOB,
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_ghost_event PRIMARY KEY (ghost_event_id),
    CONSTRAINT fk_bat_ghost_event_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_ghost_event_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_ghost_event_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL
);
CREATE INDEX ix_bat_ghost_event_execution ON bat_ghost_event (execution_id, ghost_status);
CREATE INDEX ix_bat_ghost_event_job ON bat_ghost_event (job_id, detected_at);
CREATE INDEX ix_bat_ghost_event_worker ON bat_ghost_event (worker_id, detected_at);
COMMENT ON TABLE bat_ghost_event IS 'BAT 배치 ghost 감지와 조치 이력';
COMMENT ON COLUMN bat_ghost_event.ghost_event_id IS '배치 ghost 이벤트 순번';
COMMENT ON COLUMN bat_ghost_event.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_ghost_event.spring_batch_execution_id IS 'Spring Batch JobExecution ID';
COMMENT ON COLUMN bat_ghost_event.job_id IS '배치 Job ID';
COMMENT ON COLUMN bat_ghost_event.instance_id IS '서버 인스턴스 ID';
COMMENT ON COLUMN bat_ghost_event.worker_id IS 'worker ID';
COMMENT ON COLUMN bat_ghost_event.ghost_status IS 'ghost 이벤트 상태';
COMMENT ON COLUMN bat_ghost_event.detected_reason IS '감지 사유';
COMMENT ON COLUMN bat_ghost_event.action_type IS '조치 유형';
COMMENT ON COLUMN bat_ghost_event.action_reason IS '조치 사유';
COMMENT ON COLUMN bat_ghost_event.action_by IS '조치 운영자';
COMMENT ON COLUMN bat_ghost_event.detected_at IS '감지 일시';
COMMENT ON COLUMN bat_ghost_event.action_at IS '조치 일시';
COMMENT ON COLUMN bat_ghost_event.lock_released_yn IS '잠금 해제 여부';
COMMENT ON COLUMN bat_ghost_event.retryable_yn IS '재수행 가능 여부';
COMMENT ON COLUMN bat_ghost_event.before_data IS '조치 전 데이터';
COMMENT ON COLUMN bat_ghost_event.after_data IS '조치 후 데이터';
COMMENT ON COLUMN bat_ghost_event.created_by IS '등록자';
COMMENT ON COLUMN bat_ghost_event.created_at IS '등록일시';
COMMENT ON COLUMN bat_ghost_event.updated_by IS '수정자';
COMMENT ON COLUMN bat_ghost_event.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_ghost_event BEFORE UPDATE ON bat_ghost_event FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_schedule_trigger (
    schedule_id VARCHAR2(100 CHAR) NOT NULL,
    scheduled_fire_at TIMESTAMP(6) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    execution_id NUMBER(19),
    trigger_status VARCHAR2(30 CHAR) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    job_id VARCHAR2(100 CHAR) NOT NULL,
    definition_version NUMBER(19) NOT NULL,
    definition_checksum VARCHAR2(128 CHAR) NOT NULL,
    business_date DATE NOT NULL,
    fire_zone VARCHAR2(50 CHAR) NOT NULL,
    idempotency_key VARCHAR2(200 CHAR) NOT NULL,
    dispatch_owner VARCHAR2(160 CHAR),
    dispatch_token NUMBER(19),
    dispatch_lease_until TIMESTAMP(6),
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error_code VARCHAR2(100 CHAR),
    last_error_at TIMESTAMP(6),
    dispatched_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_schedule_trigger PRIMARY KEY (schedule_id, scheduled_fire_at),
    CONSTRAINT uq_bat_schedule_trigger_idem UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_schedule_trigger_schedule FOREIGN KEY (schedule_id) REFERENCES bat_schedule (schedule_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_schedule_trigger_dispatch ON bat_schedule_trigger (trigger_status, dispatch_lease_until, scheduled_fire_at);
COMMENT ON TABLE bat_schedule_trigger IS 'BAT scheduled trigger evidence';
COMMENT ON COLUMN bat_schedule_trigger.schedule_id IS 'Schedule identifier';
COMMENT ON COLUMN bat_schedule_trigger.scheduled_fire_at IS 'Planned fire time';
COMMENT ON COLUMN bat_schedule_trigger.fencing_token IS 'Scheduler fencing token';
COMMENT ON COLUMN bat_schedule_trigger.execution_id IS 'Created execution identifier';
COMMENT ON COLUMN bat_schedule_trigger.trigger_status IS 'Trigger result status';
COMMENT ON COLUMN bat_schedule_trigger.created_at IS 'Trigger record time';
COMMENT ON COLUMN bat_schedule_trigger.job_id IS '실행 Job ID';
COMMENT ON COLUMN bat_schedule_trigger.definition_version IS '고정 Job definition version';
COMMENT ON COLUMN bat_schedule_trigger.definition_checksum IS '고정 Job definition checksum';
COMMENT ON COLUMN bat_schedule_trigger.business_date IS '실행 영업일';
COMMENT ON COLUMN bat_schedule_trigger.fire_zone IS '예정시각 timezone';
COMMENT ON COLUMN bat_schedule_trigger.idempotency_key IS '재시작에도 고정되는 실행 멱등키';
COMMENT ON COLUMN bat_schedule_trigger.dispatch_owner IS '현재 dispatch lease owner';
COMMENT ON COLUMN bat_schedule_trigger.dispatch_token IS 'dispatch fencing token';
COMMENT ON COLUMN bat_schedule_trigger.dispatch_lease_until IS 'dispatch lease 만료시각';
COMMENT ON COLUMN bat_schedule_trigger.attempt_count IS 'dispatch 시도 횟수';
COMMENT ON COLUMN bat_schedule_trigger.last_error_code IS '최근 오류 코드';
COMMENT ON COLUMN bat_schedule_trigger.last_error_at IS '최근 오류 시각';
COMMENT ON COLUMN bat_schedule_trigger.dispatched_at IS '실제 dispatch 완료 시각';
COMMENT ON COLUMN bat_schedule_trigger.updated_at IS '최근 상태 변경 시각';

CREATE TABLE bat_step_execution (
    step_execution_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    execution_id NUMBER(19) NOT NULL,
    spring_batch_step_execution_id NUMBER(19),
    worker_id VARCHAR2(160 CHAR),
    step_name VARCHAR2(150 CHAR) NOT NULL,
    execution_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'READY',
    start_time TIMESTAMP(3),
    end_time TIMESTAMP(3),
    read_count NUMBER(19) NOT NULL DEFAULT 0,
    write_count NUMBER(19) NOT NULL DEFAULT 0,
    skip_count NUMBER(19) NOT NULL DEFAULT 0,
    total_count NUMBER(19) NOT NULL DEFAULT 0,
    processed_count NUMBER(19) NOT NULL DEFAULT 0,
    success_count NUMBER(19) NOT NULL DEFAULT 0,
    failure_count NUMBER(19) NOT NULL DEFAULT 0,
    retry_count NUMBER(19) NOT NULL DEFAULT 0,
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    avg_elapsed_ms NUMBER(19) NOT NULL DEFAULT 0,
    max_elapsed_ms NUMBER(19) NOT NULL DEFAULT 0,
    last_heartbeat_at TIMESTAMP(3),
    error_message CLOB,
    step_log CLOB,
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_step_execution PRIMARY KEY (step_execution_id),
    CONSTRAINT fk_bat_step_execution_parent FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_step_execution_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL
);
CREATE INDEX ix_bat_step_execution_parent ON bat_step_execution (execution_id, step_name);
CREATE INDEX ix_bat_step_execution_spring ON bat_step_execution (spring_batch_step_execution_id);
CREATE INDEX ix_bat_step_execution_worker ON bat_step_execution (worker_id, start_time);
CREATE INDEX ix_bat_step_execution_heartbeat ON bat_step_execution (execution_status, last_heartbeat_at);
COMMENT ON TABLE bat_step_execution IS 'BAT 배치 Step 실행 이력';
COMMENT ON COLUMN bat_step_execution.step_execution_id IS '배치 Step 실행 순번';
COMMENT ON COLUMN bat_step_execution.execution_id IS '배치 실행 순번';
COMMENT ON COLUMN bat_step_execution.spring_batch_step_execution_id IS 'Spring Batch StepExecution ID';
COMMENT ON COLUMN bat_step_execution.worker_id IS '실행 worker ID';
COMMENT ON COLUMN bat_step_execution.step_name IS 'Step 이름';
COMMENT ON COLUMN bat_step_execution.execution_status IS '실행 상태';
COMMENT ON COLUMN bat_step_execution.start_time IS '시작 일시';
COMMENT ON COLUMN bat_step_execution.end_time IS '종료 일시';
COMMENT ON COLUMN bat_step_execution.read_count IS '읽은 건수';
COMMENT ON COLUMN bat_step_execution.write_count IS '처리 건수';
COMMENT ON COLUMN bat_step_execution.skip_count IS '건너뛴 건수';
COMMENT ON COLUMN bat_step_execution.total_count IS '전체 처리 대상 건수';
COMMENT ON COLUMN bat_step_execution.processed_count IS '처리 완료 건수';
COMMENT ON COLUMN bat_step_execution.success_count IS '성공 처리 건수';
COMMENT ON COLUMN bat_step_execution.failure_count IS '실패 처리 건수';
COMMENT ON COLUMN bat_step_execution.retry_count IS '재시도 또는 rollback 건수';
COMMENT ON COLUMN bat_step_execution.progress_rate IS '진행률';
COMMENT ON COLUMN bat_step_execution.tps IS '초당 처리 건수';
COMMENT ON COLUMN bat_step_execution.avg_elapsed_ms IS '평균 처리 시간 밀리초';
COMMENT ON COLUMN bat_step_execution.max_elapsed_ms IS '최대 처리 시간 밀리초';
COMMENT ON COLUMN bat_step_execution.last_heartbeat_at IS 'Step 메타 마지막 heartbeat 일시';
COMMENT ON COLUMN bat_step_execution.error_message IS '오류 메시지';
COMMENT ON COLUMN bat_step_execution.step_log IS 'Step 로그';
COMMENT ON COLUMN bat_step_execution.created_by IS '등록자';
COMMENT ON COLUMN bat_step_execution.created_at IS '등록일시';
COMMENT ON COLUMN bat_step_execution.updated_by IS '수정자';
COMMENT ON COLUMN bat_step_execution.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_step_execution BEFORE UPDATE ON bat_step_execution FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID NUMBER(19) NOT NULL,
    SHORT_CONTEXT VARCHAR2(2500 CHAR) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT pk_BATCH_STEP_EXECUTION_CONTEXT PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);
COMMENT ON TABLE BATCH_STEP_EXECUTION_CONTEXT IS 'Spring Batch 표준 Step 컨텍스트 저장소';
COMMENT ON COLUMN BATCH_STEP_EXECUTION_CONTEXT.STEP_EXECUTION_ID IS 'Spring Batch StepExecution 순번';
COMMENT ON COLUMN BATCH_STEP_EXECUTION_CONTEXT.SHORT_CONTEXT IS '짧은 실행 컨텍스트';
COMMENT ON COLUMN BATCH_STEP_EXECUTION_CONTEXT.SERIALIZED_CONTEXT IS '직렬화 실행 컨텍스트';

CREATE TABLE bat_center_cut_item (
    center_cut_item_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    center_cut_job_id VARCHAR2(100 CHAR) NOT NULL,
    center_cut_execution_id VARCHAR2(80 CHAR),
    business_key VARCHAR2(200 CHAR) NOT NULL,
    business_date DATE,
    item_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'READY',
    transaction_id CHAR(34 CHAR),
    transaction_segment_id VARCHAR2(120 CHAR),
    parent_segment_id VARCHAR2(120 CHAR),
    item_payload CLOB,
    retry_count NUMBER(10) NOT NULL DEFAULT 0,
    last_error_message VARCHAR2(1000 CHAR),
    started_at TIMESTAMP(3),
    completed_at TIMESTAMP(3),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_center_cut_item PRIMARY KEY (center_cut_item_id),
    CONSTRAINT uk_bat_center_cut_item_execution_business UNIQUE (center_cut_execution_id, business_key),
    CONSTRAINT fk_bat_center_cut_item_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_item_execution FOREIGN KEY (center_cut_execution_id) REFERENCES bat_center_cut_execution (center_cut_execution_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_center_cut_item_status ON bat_center_cut_item (center_cut_job_id, item_status, business_date);
CREATE INDEX ix_bat_center_cut_item_transaction ON bat_center_cut_item (transaction_id, transaction_segment_id);
CREATE INDEX ix_bat_center_cut_item_parent_segment ON bat_center_cut_item (parent_segment_id);
CREATE INDEX ix_bat_center_cut_item_execution_status ON bat_center_cut_item (center_cut_execution_id, item_status, center_cut_item_id);
COMMENT ON TABLE bat_center_cut_item IS 'BAT 센터컷 처리 대상';
COMMENT ON COLUMN bat_center_cut_item.center_cut_item_id IS '센터컷 대상 순번';
COMMENT ON COLUMN bat_center_cut_item.center_cut_job_id IS '센터컷 Job ID';
COMMENT ON COLUMN bat_center_cut_item.center_cut_execution_id IS 'Center-cut execution identifier';
COMMENT ON COLUMN bat_center_cut_item.business_key IS '업무 멱등 키';
COMMENT ON COLUMN bat_center_cut_item.business_date IS '업무 기준일';
COMMENT ON COLUMN bat_center_cut_item.item_status IS '대상 상태';
COMMENT ON COLUMN bat_center_cut_item.transaction_id IS '센터컷 실행 전체가 승계하는 CPF transactionId';
COMMENT ON COLUMN bat_center_cut_item.transaction_segment_id IS '현재 센터컷 Item 실행 구간 ID';
COMMENT ON COLUMN bat_center_cut_item.parent_segment_id IS '부모 센터컷/Worker 실행 구간 ID';
COMMENT ON COLUMN bat_center_cut_item.item_payload IS '처리 입력 payload';
COMMENT ON COLUMN bat_center_cut_item.retry_count IS '재처리 횟수';
COMMENT ON COLUMN bat_center_cut_item.last_error_message IS '마지막 오류 메시지';
COMMENT ON COLUMN bat_center_cut_item.started_at IS '처리 시작 일시';
COMMENT ON COLUMN bat_center_cut_item.completed_at IS '처리 완료 일시';
COMMENT ON COLUMN bat_center_cut_item.created_by IS '등록자';
COMMENT ON COLUMN bat_center_cut_item.created_at IS '등록일시';
COMMENT ON COLUMN bat_center_cut_item.updated_by IS '수정자';
COMMENT ON COLUMN bat_center_cut_item.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_item BEFORE UPDATE ON bat_center_cut_item FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_center_cut_rate_window (
    center_cut_execution_id VARCHAR2(80 CHAR) NOT NULL,
    window_second NUMBER(19) NOT NULL,
    admitted_count NUMBER(10) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_center_cut_rate_window PRIMARY KEY (center_cut_execution_id, window_second),
    CONSTRAINT fk_bat_center_cut_rate_execution FOREIGN KEY (center_cut_execution_id) REFERENCES bat_center_cut_execution (center_cut_execution_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_center_cut_rate_window IS 'BAT center-cut global rate window';
COMMENT ON COLUMN bat_center_cut_rate_window.center_cut_execution_id IS 'Center-cut execution identifier';
COMMENT ON COLUMN bat_center_cut_rate_window.window_second IS 'UTC epoch-second rate window';
COMMENT ON COLUMN bat_center_cut_rate_window.admitted_count IS 'Items admitted in this window';
COMMENT ON COLUMN bat_center_cut_rate_window.updated_at IS 'Last bucket update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_rate_window BEFORE UPDATE ON bat_center_cut_rate_window FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_center_cut_claim (
    center_cut_item_id NUMBER(19),
    runner_id VARCHAR2(160 CHAR) NOT NULL,
    pool_id VARCHAR2(80 CHAR),
    claim_token VARCHAR2(80 CHAR) NOT NULL,
    claim_status VARCHAR2(30 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL,
    lease_until TIMESTAMP(6) NOT NULL,
    last_heartbeat_at TIMESTAMP(6) NOT NULL,
    attempt_no NUMBER(10) NOT NULL DEFAULT 1,
    takeover_count NUMBER(10) NOT NULL DEFAULT 0,
    released_at TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_bat_center_cut_claim PRIMARY KEY (center_cut_item_id),
    CONSTRAINT claim_token UNIQUE (claim_token),
    CONSTRAINT fk_bat_center_cut_claim_item FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item (center_cut_item_id) ON DELETE CASCADE
);
COMMENT ON TABLE bat_center_cut_claim IS 'BAT center-cut item lease claim';
COMMENT ON COLUMN bat_center_cut_claim.center_cut_item_id IS 'Claimed center-cut item identifier';
COMMENT ON COLUMN bat_center_cut_claim.runner_id IS 'Owning runner identifier';
COMMENT ON COLUMN bat_center_cut_claim.pool_id IS 'Owning runner pool identifier';
COMMENT ON COLUMN bat_center_cut_claim.claim_token IS 'Unique claim token';
COMMENT ON COLUMN bat_center_cut_claim.claim_status IS 'Claim lifecycle status';
COMMENT ON COLUMN bat_center_cut_claim.fencing_token IS 'Monotonic claim fencing token';
COMMENT ON COLUMN bat_center_cut_claim.lease_until IS 'Claim lease expiry time';
COMMENT ON COLUMN bat_center_cut_claim.last_heartbeat_at IS 'Claim heartbeat time';
COMMENT ON COLUMN bat_center_cut_claim.attempt_no IS 'Claim attempt number';
COMMENT ON COLUMN bat_center_cut_claim.takeover_count IS 'Claim takeover count';
COMMENT ON COLUMN bat_center_cut_claim.released_at IS 'Claim release time';
COMMENT ON COLUMN bat_center_cut_claim.updated_at IS 'Last claim update time';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_claim BEFORE UPDATE ON bat_center_cut_claim FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE bat_center_cut_result (
    center_cut_result_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    center_cut_item_id NUMBER(19) NOT NULL,
    center_cut_job_id VARCHAR2(100 CHAR) NOT NULL,
    result_status VARCHAR2(30 CHAR) NOT NULL,
    result_payload CLOB,
    result_message VARCHAR2(1000 CHAR),
    transaction_id CHAR(34 CHAR),
    transaction_segment_id VARCHAR2(120 CHAR),
    parent_segment_id VARCHAR2(120 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(100 CHAR) NOT NULL DEFAULT 'BAT',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_center_cut_result PRIMARY KEY (center_cut_result_id),
    CONSTRAINT fk_bat_center_cut_result_item FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item (center_cut_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_result_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE
);
CREATE INDEX ix_bat_center_cut_result_item ON bat_center_cut_result (center_cut_item_id, result_status);
CREATE INDEX ix_bat_center_cut_result_transaction ON bat_center_cut_result (transaction_id, transaction_segment_id);
CREATE INDEX ix_bat_center_cut_result_parent_segment ON bat_center_cut_result (parent_segment_id);
CREATE INDEX ix_bat_center_cut_result_job ON bat_center_cut_result (center_cut_job_id, created_at);
COMMENT ON TABLE bat_center_cut_result IS 'BAT 센터컷 처리 결과';
COMMENT ON COLUMN bat_center_cut_result.center_cut_result_id IS '센터컷 결과 순번';
COMMENT ON COLUMN bat_center_cut_result.center_cut_item_id IS '센터컷 대상 순번';
COMMENT ON COLUMN bat_center_cut_result.center_cut_job_id IS '센터컷 Job ID';
COMMENT ON COLUMN bat_center_cut_result.result_status IS '처리 결과 상태';
COMMENT ON COLUMN bat_center_cut_result.result_payload IS '처리 결과 payload';
COMMENT ON COLUMN bat_center_cut_result.result_message IS '처리 결과 메시지';
COMMENT ON COLUMN bat_center_cut_result.transaction_id IS '센터컷 실행 전체가 승계하는 CPF transactionId';
COMMENT ON COLUMN bat_center_cut_result.transaction_segment_id IS '결과를 생성한 거래 구간 ID';
COMMENT ON COLUMN bat_center_cut_result.parent_segment_id IS '부모 센터컷/Worker 실행 구간 ID';
COMMENT ON COLUMN bat_center_cut_result.created_by IS '등록자';
COMMENT ON COLUMN bat_center_cut_result.created_at IS '등록일시';
COMMENT ON COLUMN bat_center_cut_result.updated_by IS '수정자';
COMMENT ON COLUMN bat_center_cut_result.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_bat_center_cut_result BEFORE UPDATE ON bat_center_cut_result FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/


-- R4 BAT dangerous-operation approval/idempotency ledger
CREATE TABLE bat_operation_request (
    idempotency_key VARCHAR2(120 CHAR) NOT NULL, request_hash CHAR(64 CHAR) NOT NULL,
    operation_type VARCHAR2(80 CHAR) NOT NULL, target_type VARCHAR2(80 CHAR) NOT NULL, target_id VARCHAR2(200 CHAR) NOT NULL,
    action_type VARCHAR2(100 CHAR) NOT NULL, approval_request_id VARCHAR2(120 CHAR) NOT NULL, requested_by VARCHAR2(50 CHAR) NOT NULL,
    expected_version NUMBER(19), request_state VARCHAR2(30 CHAR) DEFAULT 'RESERVED' NOT NULL, result_payload CLOB,
    failure_code VARCHAR2(80 CHAR), failure_message VARCHAR2(1000 CHAR), completed_at TIMESTAMP(3),
    created_by VARCHAR2(50 CHAR) DEFAULT 'BAT' NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR2(50 CHAR) DEFAULT 'BAT' NOT NULL, updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_bat_operation_request PRIMARY KEY (idempotency_key),
    CONSTRAINT ck_bat_operation_request_state CHECK (request_state IN ('RESERVED','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_bat_operation_request_hash CHECK (LENGTH(request_hash)=64)
);
CREATE INDEX ix_bat_operation_request_target ON bat_operation_request(target_type,target_id,created_at);
CREATE INDEX ix_bat_operation_request_state ON bat_operation_request(request_state,updated_at);
