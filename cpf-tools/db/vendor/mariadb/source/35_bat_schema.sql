-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=mariadb
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=batDB
USE batDB;
CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT '실행 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT '실행 상태',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    CONSTRAINT pk_BATCH_JOB_EXECUTION PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    CONSTRAINT pk_BATCH_JOB_EXECUTION_CONTEXT PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    PARAMETER_NAME VARCHAR(100) NOT NULL COMMENT '파라미터 이름',
    PARAMETER_TYPE VARCHAR(100) NOT NULL COMMENT '파라미터 Java 유형',
    PARAMETER_VALUE VARCHAR(2500) NULL COMMENT '파라미터 값',
    IDENTIFYING CHAR(1) NOT NULL COMMENT 'JobInstance 식별 파라미터 여부',
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 파라미터 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_NAME VARCHAR(100) NOT NULL COMMENT 'Spring Batch Job 이름',
    JOB_KEY VARCHAR(32) NOT NULL COMMENT 'Job 파라미터 식별 키',
    CONSTRAINT pk_BATCH_JOB_INSTANCE PRIMARY KEY (JOB_INSTANCE_ID),
    CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobInstance 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    VERSION BIGINT NOT NULL COMMENT '낙관적 잠금 버전',
    STEP_NAME VARCHAR(100) NOT NULL COMMENT 'Step 이름',
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT 'Step 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT 'Step 상태',
    COMMIT_COUNT BIGINT NULL COMMENT '커밋 횟수',
    READ_COUNT BIGINT NULL COMMENT '읽은 건수',
    FILTER_COUNT BIGINT NULL COMMENT '필터 건수',
    WRITE_COUNT BIGINT NULL COMMENT '쓴 건수',
    READ_SKIP_COUNT BIGINT NULL COMMENT '읽기 skip 건수',
    WRITE_SKIP_COUNT BIGINT NULL COMMENT '쓰기 skip 건수',
    PROCESS_SKIP_COUNT BIGINT NULL COMMENT '처리 skip 건수',
    ROLLBACK_COUNT BIGINT NULL COMMENT 'rollback 건수',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    CONSTRAINT pk_BATCH_STEP_EXECUTION PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 StepExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    CONSTRAINT pk_BATCH_STEP_EXECUTION_CONTEXT PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Step 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS bat_center_cut_claim (
    center_cut_item_id BIGINT NULL COMMENT 'Claimed center-cut item identifier',
    runner_id VARCHAR(160) NOT NULL COMMENT 'Owning runner identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Owning runner pool identifier',
    claim_token VARCHAR(80) NOT NULL COMMENT 'Unique claim token',
    claim_status VARCHAR(30) NOT NULL COMMENT 'Claim lifecycle status',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic claim fencing token',
    lease_until DATETIME(6) NOT NULL COMMENT 'Claim lease expiry time',
    last_heartbeat_at DATETIME(6) NOT NULL COMMENT 'Claim heartbeat time',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT 'Claim attempt number',
    takeover_count INT NOT NULL DEFAULT 0 COMMENT 'Claim takeover count',
    released_at DATETIME(6) NULL COMMENT 'Claim release time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last claim update time',
    CONSTRAINT pk_bat_center_cut_claim PRIMARY KEY (center_cut_item_id),
    CONSTRAINT claim_token UNIQUE (claim_token),
    CONSTRAINT fk_bat_center_cut_claim_item FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item (center_cut_item_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut item lease claim';

CREATE TABLE IF NOT EXISTS bat_center_cut_execution (
    center_cut_execution_id VARCHAR(80) NOT NULL COMMENT 'Center-cut execution identifier',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT 'Center-cut job definition identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Execution idempotency key',
    execution_state VARCHAR(30) NOT NULL COMMENT 'Center-cut execution state',
    parameter_ciphertext LONGTEXT NOT NULL COMMENT 'Encrypted immutable parameter snapshot',
    parameter_hash VARCHAR(64) NOT NULL COMMENT 'Parameter snapshot SHA-256',
    parameter_schema_version VARCHAR(80) NOT NULL COMMENT 'Parameter schema version',
    target_cursor VARCHAR(1000) NULL COMMENT 'Last generated target cursor',
    target_complete_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Target generation completion flag',
    target_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Generated target count',
    tps_limit INT NOT NULL DEFAULT 0 COMMENT 'Global transactions-per-second limit',
    concurrency_limit INT NOT NULL DEFAULT 1 COMMENT 'Global runner concurrency limit',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Processed item count',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Successful item count',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Failed item count',
    unknown_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Unknown-result item count',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    parent_segment_id VARCHAR(120) NULL COMMENT 'Parent trace segment identifier',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Execution requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory execution reason',
    last_error_message VARCHAR(1000) NULL COMMENT 'Last execution error detail',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Execution request time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last execution state update time',
    completed_at DATETIME(6) NULL COMMENT 'Execution completion time',
    CONSTRAINT pk_bat_center_cut_execution PRIMARY KEY (center_cut_execution_id),
    CONSTRAINT uk_bat_center_cut_execution_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_center_cut_execution_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id),
    INDEX ix_bat_center_cut_execution_job_state (center_cut_job_id, execution_state, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut immutable execution policy';

CREATE TABLE IF NOT EXISTS bat_center_cut_item (
    center_cut_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    center_cut_execution_id VARCHAR(80) NULL COMMENT 'Center-cut execution identifier',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NULL COMMENT '업무 기준일',
    item_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태',
    transaction_id CHAR(34) NULL COMMENT '센터컷 실행 전체가 승계하는 CPF transactionId',
    transaction_segment_id VARCHAR(120) NULL COMMENT '현재 센터컷 Item 실행 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '부모 센터컷/Worker 실행 구간 ID',
    item_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    started_at DATETIME(3) NULL COMMENT '처리 시작 일시',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_center_cut_item PRIMARY KEY (center_cut_item_id),
    CONSTRAINT uk_bat_center_cut_item_execution_business UNIQUE (center_cut_execution_id, business_key),
    CONSTRAINT fk_bat_center_cut_item_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_item_execution FOREIGN KEY (center_cut_execution_id) REFERENCES bat_center_cut_execution (center_cut_execution_id) ON DELETE CASCADE,
    INDEX ix_bat_center_cut_item_status (center_cut_job_id, item_status, business_date),
    INDEX ix_bat_center_cut_item_transaction (transaction_id, transaction_segment_id),
    INDEX ix_bat_center_cut_item_parent_segment (parent_segment_id),
    INDEX ix_bat_center_cut_item_execution_status (center_cut_execution_id, item_status, center_cut_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 대상';

CREATE TABLE IF NOT EXISTS bat_center_cut_job (
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    batch_job_id VARCHAR(100) NULL COMMENT '연결된 BAT 배치 Job ID',
    center_cut_job_name VARCHAR(150) NOT NULL COMMENT '센터컷 Job 명',
    provider_key VARCHAR(100) NOT NULL COMMENT '대상 조회 Provider 식별자',
    handler_key VARCHAR(100) NOT NULL COMMENT '처리 Handler 식별자',
    chunk_size INT NOT NULL DEFAULT 100 COMMENT '한 번에 조회할 대상 건수',
    retry_limit INT NOT NULL DEFAULT 3 COMMENT '최대 재처리 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    description VARCHAR(500) NULL COMMENT '센터컷 Job 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_center_cut_job PRIMARY KEY (center_cut_job_id),
    CONSTRAINT fk_bat_center_cut_job_batch FOREIGN KEY (batch_job_id) REFERENCES bat_job (job_id) ON DELETE SET NULL,
    INDEX ix_bat_center_cut_job_batch (batch_job_id, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 Job 정의';

CREATE TABLE IF NOT EXISTS bat_center_cut_parameter (
    parameter_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 파라미터 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    parameter_key VARCHAR(100) NOT NULL COMMENT '파라미터 키',
    parameter_value VARCHAR(1000) NULL COMMENT '파라미터 값',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_center_cut_parameter PRIMARY KEY (parameter_id),
    CONSTRAINT uk_bat_center_cut_parameter UNIQUE (center_cut_job_id, parameter_key),
    CONSTRAINT fk_bat_center_cut_parameter_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 파라미터';

CREATE TABLE IF NOT EXISTS bat_center_cut_rate_window (
    center_cut_execution_id VARCHAR(80) NOT NULL COMMENT 'Center-cut execution identifier',
    window_second BIGINT NOT NULL COMMENT 'UTC epoch-second rate window',
    admitted_count INT NOT NULL DEFAULT 0 COMMENT 'Items admitted in this window',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last bucket update time',
    CONSTRAINT pk_bat_center_cut_rate_window PRIMARY KEY (center_cut_execution_id, window_second),
    CONSTRAINT fk_bat_center_cut_rate_execution FOREIGN KEY (center_cut_execution_id) REFERENCES bat_center_cut_execution (center_cut_execution_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut global rate window';

CREATE TABLE IF NOT EXISTS bat_center_cut_result (
    center_cut_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 결과 순번',
    center_cut_item_id BIGINT NOT NULL COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    transaction_id CHAR(34) NULL COMMENT '센터컷 실행 전체가 승계하는 CPF transactionId',
    transaction_segment_id VARCHAR(120) NULL COMMENT '결과를 생성한 거래 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '부모 센터컷/Worker 실행 구간 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_center_cut_result PRIMARY KEY (center_cut_result_id),
    CONSTRAINT fk_bat_center_cut_result_item FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item (center_cut_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_result_job FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job (center_cut_job_id) ON DELETE CASCADE,
    INDEX ix_bat_center_cut_result_item (center_cut_item_id, result_status),
    INDEX ix_bat_center_cut_result_transaction (transaction_id, transaction_segment_id),
    INDEX ix_bat_center_cut_result_parent_segment (parent_segment_id),
    INDEX ix_bat_center_cut_result_job (center_cut_job_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 결과';

CREATE TABLE IF NOT EXISTS bat_deployment_cell (
    cell_id VARCHAR(120) NULL COMMENT 'Deployment cell identifier',
    environment_id VARCHAR(80) NOT NULL COMMENT 'Target environment identifier',
    runtime_role VARCHAR(40) NOT NULL COMMENT 'Target runtime role',
    service_id VARCHAR(120) NOT NULL COMMENT 'Target service identifier',
    manifest_version VARCHAR(80) NOT NULL COMMENT 'Desired manifest version',
    manifest_hash VARCHAR(128) NOT NULL COMMENT 'Desired manifest checksum',
    desired_state VARCHAR(32) NOT NULL COMMENT 'Desired cell state',
    desired_instance_count INT NOT NULL DEFAULT 1 COMMENT 'Desired runtime instance count',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Cell registration time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last desired-state update time',
    CONSTRAINT pk_bat_deployment_cell PRIMARY KEY (cell_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell desired state';

CREATE TABLE IF NOT EXISTS bat_deployment_execution (
    deployment_id VARCHAR(80) NOT NULL COMMENT 'Deployment execution identifier',
    cell_id VARCHAR(120) NOT NULL COMMENT 'Target deployment cell identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Deployment idempotency key',
    from_version VARCHAR(80) NULL COMMENT 'Previous artifact version',
    to_version VARCHAR(80) NOT NULL COMMENT 'Target artifact version',
    strategy_code VARCHAR(32) NOT NULL COMMENT 'ROLLING/CANARY/BLUE_GREEN strategy',
    execution_state VARCHAR(40) NOT NULL COMMENT 'Deployment execution state',
    failure_stage VARCHAR(80) NULL COMMENT 'Failed deployment stage',
    result_message VARCHAR(4000) NULL COMMENT 'Deployment result detail',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Deployment requester',
    approved_by VARCHAR(120) NOT NULL COMMENT 'Deployment approver',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory deployment reason',
    started_at DATETIME(6) NULL COMMENT 'Deployment start time',
    finished_at DATETIME(6) NULL COMMENT 'Deployment finish time',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Deployment record time',
    CONSTRAINT pk_bat_deployment_execution PRIMARY KEY (deployment_id),
    CONSTRAINT uk_bat_deployment_execution_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_deployment_execution_cell FOREIGN KEY (cell_id) REFERENCES bat_deployment_cell (cell_id),
    INDEX ix_bat_deployment_execution_cell_state (cell_id, execution_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT approved deployment execution';

CREATE TABLE IF NOT EXISTS bat_deployment_instance (
    cell_id VARCHAR(120) NOT NULL COMMENT 'Deployment cell identifier',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Runtime instance identifier',
    host_alias VARCHAR(160) NOT NULL COMMENT 'Target host alias',
    port_no INT NOT NULL COMMENT 'Runtime service port',
    profile_name VARCHAR(80) NOT NULL COMMENT 'Runtime profile name',
    zone_id VARCHAR(80) NULL COMMENT 'Availability zone identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Runtime pool identifier',
    agent_base_url VARCHAR(500) NOT NULL COMMENT 'Approved host-agent base URL',
    config_ref VARCHAR(1000) NULL COMMENT 'External configuration reference',
    desired_state VARCHAR(32) NOT NULL COMMENT 'Desired instance state',
    CONSTRAINT pk_bat_deployment_instance PRIMARY KEY (cell_id, instance_id),
    CONSTRAINT uk_bat_deployment_instance_id UNIQUE (instance_id),
    CONSTRAINT fk_bat_deployment_instance_cell FOREIGN KEY (cell_id) REFERENCES bat_deployment_cell (cell_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell instance projection';

CREATE TABLE IF NOT EXISTS bat_deployment_instance_result (
    deployment_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Instance result identifier',
    deployment_id VARCHAR(80) NOT NULL COMMENT 'Deployment execution identifier',
    sequence_no INT NOT NULL COMMENT 'Ordered result sequence',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Target runtime instance identifier',
    stage_code VARCHAR(80) NOT NULL COMMENT 'Deployment stage code',
    result_state VARCHAR(40) NOT NULL COMMENT 'Instance stage result state',
    result_message VARCHAR(4000) NULL COMMENT 'Instance stage result detail',
    recorded_at DATETIME(6) NOT NULL COMMENT 'Result record time',
    CONSTRAINT pk_bat_deployment_instance_result PRIMARY KEY (deployment_result_id),
    CONSTRAINT uk_bat_deployment_instance_result UNIQUE (deployment_id, sequence_no),
    CONSTRAINT fk_bat_deployment_instance_result_execution FOREIGN KEY (deployment_id) REFERENCES bat_deployment_execution (deployment_id) ON DELETE CASCADE,
    INDEX ix_bat_deployment_instance_result_instance (instance_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT per-instance deployment result';

CREATE TABLE IF NOT EXISTS bat_deployment_lock (
    cell_id VARCHAR(120) NULL COMMENT 'Locked deployment cell identifier',
    owner_deployment_id VARCHAR(80) NOT NULL COMMENT 'Lock owner deployment identifier',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic deployment fencing token',
    locked_at DATETIME(6) NOT NULL COMMENT 'Lock acquisition time',
    expires_at DATETIME(6) NOT NULL COMMENT 'Lock expiry time',
    CONSTRAINT pk_bat_deployment_lock PRIMARY KEY (cell_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell lease lock';

CREATE TABLE IF NOT EXISTS bat_deployment_plan (
    plan_id VARCHAR(80) NULL COMMENT 'Deployment plan identifier',
    cell_id VARCHAR(120) NOT NULL COMMENT 'Target deployment cell identifier',
    manifest_json LONGTEXT NOT NULL COMMENT 'Immutable deployment manifest snapshot',
    manifest_hash VARCHAR(128) NOT NULL COMMENT 'Deployment manifest checksum',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Plan requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory deployment reason',
    plan_state VARCHAR(40) NOT NULL COMMENT 'Deployment plan lifecycle state',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Plan request time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last plan state update time',
    CONSTRAINT pk_bat_deployment_plan PRIMARY KEY (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment plan';

CREATE TABLE IF NOT EXISTS bat_execution (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    job_parameters VARCHAR(2000) NULL COMMENT '배치 파라미터',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    spring_batch_job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID',
    business_date DATE NULL COMMENT 'JobInstance 시작 시 확정한 업무일자',
    run_id VARCHAR(120) NULL COMMENT '최초 실행 회차 ID',
    rerun_id VARCHAR(120) NULL COMMENT '운영 재수행 ID',
    original_job_execution_id BIGINT NULL COMMENT '재시작 기준 원 JobExecution ID',
    restart_attempt INT NOT NULL DEFAULT 0 COMMENT '동일 JobInstance 재시작 회차',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    required_worker_version VARCHAR(80) NULL COMMENT '실행에 필요한 worker 버전',
    required_capability VARCHAR(120) NULL COMMENT '실행에 필요한 worker capability',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    transaction_segment_id VARCHAR(120) NULL COMMENT '배치 Job 거래 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID',
    job_log_relative_path VARCHAR(1000) NULL COMMENT 'CPF_LOG_ROOT 기준 JobInstance 로그 상대 경로',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    stop_requested_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '운영 중지 요청 여부',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT '실행 메타 마지막 heartbeat 일시',
    current_step_name VARCHAR(150) NULL COMMENT '현재 실행 중인 Step 이름',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    requested_by VARCHAR(100) NULL COMMENT '실행 요청자',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_execution PRIMARY KEY (execution_id),
    CONSTRAINT fk_bat_execution_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_execution_instance FOREIGN KEY (batch_instance_id) REFERENCES bat_instance (instance_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL,
    INDEX ix_bat_execution_job_time (job_id, start_time),
    INDEX ix_bat_execution_status (execution_status, start_time),
    INDEX ix_bat_execution_spring (spring_batch_execution_id),
    INDEX ix_bat_execution_job_instance (spring_batch_job_instance_id, business_date),
    INDEX ix_bat_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_bat_execution_server (server_instance_id, start_time),
    INDEX ix_bat_execution_claim (execution_status, required_worker_version, required_capability, execution_id),
    INDEX ix_bat_execution_transaction (transaction_id),
    INDEX ix_bat_execution_segment (transaction_segment_id, parent_segment_id),
    INDEX ix_bat_execution_heartbeat (execution_status, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 실행 이력';

CREATE TABLE IF NOT EXISTS bat_execution_lease (
    lease_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 lease 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    worker_id VARCHAR(160) NOT NULL COMMENT '현재 lease 소유 worker ID',
    lease_token VARCHAR(80) NOT NULL COMMENT 'lease 갱신·완료 검증 토큰',
    lease_status VARCHAR(30) NOT NULL DEFAULT 'CLAIMED' COMMENT 'CLAIMED, RUNNING, RELEASED, EXPIRED 상태',
    claimed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 claim 일시',
    lease_until DATETIME(3) NOT NULL COMMENT 'lease 만료 일시',
    last_heartbeat_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '마지막 lease heartbeat 일시',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT 'claim 시도 회차',
    takeover_count INT NOT NULL DEFAULT 0 COMMENT '만료 후 다른 worker 인수 횟수',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'monotonic fencing token',
    released_at DATETIME(3) NULL COMMENT '정상 또는 실패 완료 일시',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실행 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_execution_lease PRIMARY KEY (lease_id),
    CONSTRAINT uk_bat_execution_lease_execution UNIQUE (execution_id),
    CONSTRAINT uk_bat_execution_lease_token UNIQUE (lease_token),
    CONSTRAINT fk_bat_execution_lease_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_execution_lease_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE RESTRICT,
    INDEX ix_bat_execution_lease_owner (worker_id, lease_status, lease_until),
    INDEX ix_bat_execution_lease_expire (lease_status, lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker 실행 claim과 lease';

CREATE TABLE IF NOT EXISTS bat_execution_target (
    target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 수행 대상 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    target_instance_id VARCHAR(100) NULL COMMENT '수행 대상 인스턴스 ID',
    business_date DATE NULL COMMENT '업무 기준일',
    planned_run_at DATETIME(3) NULL COMMENT '예정 수행 일시',
    dispatch_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '배정 상태',
    dispatch_reason VARCHAR(500) NULL COMMENT '배정 또는 제외 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_execution_target PRIMARY KEY (target_id),
    CONSTRAINT fk_bat_execution_target_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_execution_target_schedule FOREIGN KEY (schedule_id) REFERENCES bat_schedule (schedule_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_instance FOREIGN KEY (target_instance_id) REFERENCES bat_instance (instance_id) ON DELETE SET NULL,
    INDEX ix_bat_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_bat_execution_target_execution (execution_id),
    INDEX ix_bat_execution_target_instance (target_instance_id, dispatch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 수행 대상/대기 인스턴스';

CREATE TABLE IF NOT EXISTS bat_ghost_event (
    ghost_event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 ghost 이벤트 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    server_instance_id VARCHAR(160) NULL COMMENT '서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT 'worker ID',
    ghost_status VARCHAR(30) NOT NULL DEFAULT 'DETECTED' COMMENT 'ghost 이벤트 상태',
    detected_reason VARCHAR(1000) NOT NULL COMMENT '감지 사유',
    action_type VARCHAR(30) NULL COMMENT '조치 유형',
    action_reason VARCHAR(1000) NULL COMMENT '조치 사유',
    action_by VARCHAR(100) NULL COMMENT '조치 운영자',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '감지 일시',
    action_at DATETIME(3) NULL COMMENT '조치 일시',
    lock_released_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 해제 여부',
    retryable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재수행 가능 여부',
    before_data LONGTEXT NULL COMMENT '조치 전 데이터',
    after_data LONGTEXT NULL COMMENT '조치 후 데이터',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_ghost_event PRIMARY KEY (ghost_event_id),
    CONSTRAINT fk_bat_ghost_event_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_ghost_event_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id),
    CONSTRAINT fk_bat_ghost_event_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL,
    INDEX ix_bat_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_bat_ghost_event_job (job_id, detected_at),
    INDEX ix_bat_ghost_event_worker (worker_id, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 ghost 감지와 조치 이력';

CREATE TABLE IF NOT EXISTS bat_instance (
    instance_id VARCHAR(100) NOT NULL COMMENT '배치 인스턴스 ID',
    instance_name VARCHAR(150) NOT NULL COMMENT '배치 인스턴스명',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    server_port INT NULL COMMENT '서버 포트',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    description VARCHAR(500) NULL COMMENT '인스턴스 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_instance PRIMARY KEY (instance_id),
    INDEX ix_bat_instance_active (active_yn, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 서버 인스턴스';

CREATE TABLE IF NOT EXISTS bat_job (
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_name VARCHAR(150) NOT NULL COMMENT '배치 Job 이름',
    job_type VARCHAR(30) NOT NULL DEFAULT 'TASKLET' COMMENT '배치 Job 유형',
    description VARCHAR(500) NULL COMMENT '배치 설명',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_job PRIMARY KEY (job_id),
    INDEX ix_bat_job_use (use_yn, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Job 기준';

CREATE TABLE IF NOT EXISTS bat_job_definition_audit (
    audit_id BIGINT NOT NULL COMMENT '감사 ID',
    job_id VARCHAR(80) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    action_code VARCHAR(40) NOT NULL COMMENT '행위',
    from_state VARCHAR(20) NULL COMMENT '이전 상태',
    to_state VARCHAR(20) NULL COMMENT '다음 상태',
    reason VARCHAR(1000) NOT NULL COMMENT '사유',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발생시각',
    CONSTRAINT pk_bat_job_definition_audit PRIMARY KEY (audit_id),
    INDEX idx_bat_job_def_audit (job_id, definition_version, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Job Definition 승인·상태 감사';

CREATE TABLE IF NOT EXISTS bat_job_definition_version (
    job_id VARCHAR(80) NOT NULL COMMENT '배치 Job ID',
    definition_version BIGINT NOT NULL COMMENT '불변 Definition Version',
    job_name VARCHAR(200) NOT NULL COMMENT '배치 Job 이름',
    executor_type VARCHAR(40) NOT NULL COMMENT 'Executor 유형',
    definition_state VARCHAR(20) NOT NULL COMMENT 'Definition 상태',
    owner_domain VARCHAR(80) NOT NULL COMMENT '소유 업무영역',
    description VARCHAR(1000) NULL COMMENT '설명',
    trigger_type VARCHAR(30) NOT NULL COMMENT 'Trigger 유형',
    trigger_expression VARCHAR(500) NULL COMMENT 'Trigger 조건',
    timezone_id VARCHAR(60) NOT NULL DEFAULT 'Asia/Seoul' COMMENT 'Timezone',
    misfire_policy VARCHAR(30) NOT NULL COMMENT 'Misfire 정책',
    agent_pool VARCHAR(100) NOT NULL COMMENT 'Agent Pool',
    zone_id VARCHAR(80) NULL COMMENT '실행 Zone',
    max_concurrency INT NOT NULL DEFAULT 1 COMMENT '최대 동시 실행',
    timeout_seconds BIGINT NOT NULL DEFAULT 3600 COMMENT 'Timeout 초',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    max_attempts INT NOT NULL DEFAULT 1 COMMENT '최대 시도',
    initial_backoff_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '초기 Backoff',
    backoff_multiplier DECIMAL(10,4) NOT NULL DEFAULT 1 COMMENT 'Backoff 배수',
    max_backoff_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '최대 Backoff',
    skip_limit INT NOT NULL DEFAULT 0 COMMENT 'Skip 허용',
    unknown_result_policy VARCHAR(30) NOT NULL COMMENT '결과 불명 처리 정책',
    compensation_reference VARCHAR(200) NULL COMMENT '보상 처리 참조',
    alert_delay_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '지연 알림',
    sla_seconds BIGINT NOT NULL DEFAULT 0 COMMENT 'SLA',
    notify_failure_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '실패 알림',
    notify_missed_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '미실행 알림',
    executor_reference VARCHAR(300) NOT NULL COMMENT 'Executor 참조',
    definition_json TEXT NOT NULL COMMENT 'Definition JSON',
    checksum VARCHAR(128) NULL COMMENT 'Checksum',
    effective_from DATETIME NULL COMMENT '시행 시작',
    effective_until DATETIME NULL COMMENT '시행 종료',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_job_definition_version PRIMARY KEY (job_id, definition_version),
    CONSTRAINT ck_bat_job_def_state CHECK (definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED','RETIRED')),
    INDEX idx_bat_job_def_state (definition_state, updated_at),
    INDEX idx_bat_job_def_owner (owner_domain, job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Versioned Job Definition 정본';

CREATE TABLE IF NOT EXISTS bat_job_dependency (
    job_id VARCHAR(80) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    related_job_id VARCHAR(80) NOT NULL COMMENT '선행 Job',
    condition_code VARCHAR(40) NOT NULL COMMENT '의존 조건',
    timeout_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '대기 Timeout',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 여부',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬',
    CONSTRAINT pk_bat_job_dependency PRIMARY KEY (job_id, definition_version, related_job_id),
    CONSTRAINT ck_bat_job_dep_self CHECK (job_id <> related_job_id),
    CONSTRAINT fk_bat_job_dep_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Versioned Job Dependency';

CREATE TABLE IF NOT EXISTS bat_job_pack (
    job_pack_id VARCHAR(120) NOT NULL COMMENT 'Job-pack identifier',
    owner_domain VARCHAR(20) NOT NULL COMMENT 'Owning domain SystemCode',
    artifact_coordinate VARCHAR(240) NOT NULL COMMENT 'Job-pack artifact coordinate',
    artifact_version VARCHAR(80) NOT NULL COMMENT 'Job-pack artifact version',
    artifact_checksum VARCHAR(128) NULL COMMENT 'Job-pack artifact checksum',
    signature_present_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Artifact signature presence flag',
    platform_range VARCHAR(120) NULL COMMENT 'Compatible CPF platform range',
    manifest_json LONGTEXT NOT NULL COMMENT 'Validated job-pack manifest',
    last_registered_at DATETIME(6) NOT NULL COMMENT 'Last catalog registration time',
    CONSTRAINT pk_bat_job_pack PRIMARY KEY (job_pack_id),
    INDEX ix_bat_job_pack_owner (owner_domain, artifact_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT external job-pack catalog';

CREATE TABLE IF NOT EXISTS bat_job_pack_job (
    job_pack_id VARCHAR(120) NOT NULL COMMENT 'Owning job-pack identifier',
    job_id VARCHAR(100) NOT NULL COMMENT 'Published job identifier',
    restartable_yn CHAR(1) NOT NULL COMMENT 'Job restartability flag',
    center_cut_provider_key VARCHAR(100) NULL COMMENT 'Center-cut target provider key',
    center_cut_handler_key VARCHAR(100) NULL COMMENT 'Center-cut item handler key',
    CONSTRAINT pk_bat_job_pack_job PRIMARY KEY (job_pack_id, job_id),
    CONSTRAINT fk_bat_job_pack_job_pack FOREIGN KEY (job_pack_id) REFERENCES bat_job_pack (job_pack_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT job-pack job projection';

CREATE TABLE IF NOT EXISTS bat_job_parameter_definition (
    job_id VARCHAR(80) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    parameter_name VARCHAR(100) NOT NULL COMMENT 'Parameter 이름',
    parameter_type VARCHAR(40) NOT NULL COMMENT 'Parameter 유형',
    label_text VARCHAR(200) NULL COMMENT 'UI Label',
    description_text VARCHAR(1000) NULL COMMENT '설명',
    required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '필수 여부',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감정보 여부',
    default_value VARCHAR(1000) NULL COMMENT '기본값',
    allowed_values TEXT NULL COMMENT '허용값',
    validation_pattern VARCHAR(1000) NULL COMMENT '검증 Pattern',
    min_value DECIMAL(38,10) NULL COMMENT '최솟값',
    max_value DECIMAL(38,10) NULL COMMENT '최댓값',
    min_length INT NULL COMMENT '최소 길이',
    max_length INT NULL COMMENT '최대 길이',
    reference_type VARCHAR(80) NULL COMMENT '참조 유형',
    alias_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Alias 강제',
    runtime_override_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실행 Override',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬',
    CONSTRAINT pk_bat_job_parameter_definition PRIMARY KEY (job_id, definition_version, parameter_name),
    CONSTRAINT fk_bat_job_param_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Typed Parameter Schema';

CREATE TABLE IF NOT EXISTS bat_job_relation (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 관계 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '기준 배치 Job ID',
    related_job_id VARCHAR(100) NOT NULL COMMENT '연관 배치 Job ID',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 유형',
    trigger_condition VARCHAR(50) NOT NULL DEFAULT 'COMPLETED' COMMENT '트리거 조건',
    required_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT '필수 선행 상태',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '관계 표시 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_job_relation PRIMARY KEY (relation_id),
    CONSTRAINT uk_bat_job_relation UNIQUE (job_id, related_job_id, relation_type),
    CONSTRAINT fk_bat_job_relation_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_job_relation_related FOREIGN KEY (related_job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE,
    INDEX ix_bat_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_bat_job_relation_related (related_job_id, relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 선행/후행/트리거 관계';

CREATE TABLE IF NOT EXISTS bat_lock (
    lock_key VARCHAR(200) NOT NULL COMMENT '배치 잠금 키',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_parameters_hash VARCHAR(128) NOT NULL COMMENT 'Job 파라미터 해시',
    owner_id VARCHAR(100) NOT NULL COMMENT '잠금 소유자',
    locked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '잠금 획득 일시',
    expire_at DATETIME(3) NOT NULL COMMENT '잠금 만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_lock PRIMARY KEY (lock_key),
    INDEX ix_bat_lock_job (job_id, job_parameters_hash),
    INDEX ix_bat_lock_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 중복 실행 방지 잠금';

CREATE TABLE IF NOT EXISTS bat_on_demand_request (
    execution_request_id VARCHAR(36) NOT NULL COMMENT '온라인 접수 실행 요청 ID',
    standard_batch_id CHAR(10) NOT NULL COMMENT 'B 유형 10자리 표준 배치 ID',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 접수 방지 멱등 키',
    transaction_id CHAR(34) NOT NULL COMMENT '온라인 접수 거래 ID',
    business_date CHAR(8) NOT NULL COMMENT '배치 업무 기준일 YYYYMMDD',
    request_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED, RUNNING, COMPLETED, FAILED, RESTARTED, STOPPING 등 접수 상태',
    parameters_json LONGTEXT NULL COMMENT '검증된 배치 업무 파라미터 JSON',
    request_reason VARCHAR(500) NOT NULL COMMENT '실행 감사 사유',
    request_user VARCHAR(100) NOT NULL COMMENT '실행 요청자',
    cpf_execution_id BIGINT NULL COMMENT 'BAT 배치 실행 메타 ID',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    result_json LONGTEXT NULL COMMENT '마스킹된 실행 결과 JSON',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '민감정보가 제거된 실패 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '접수일시',
    completed_at DATETIME(3) NULL COMMENT '완료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_on_demand_request PRIMARY KEY (execution_request_id),
    CONSTRAINT uk_bat_on_demand_idempotency UNIQUE (standard_batch_id, idempotency_key),
    CONSTRAINT ck_bat_on_demand_id CHECK (standard_batch_id REGEXP '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$' AND RIGHT(standard_batch_id, 4) <> '0000'),
    CONSTRAINT ck_bat_on_demand_status CHECK (request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED', 'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED')),
    INDEX ix_bat_on_demand_status (request_status, requested_at),
    INDEX ix_bat_on_demand_transaction (transaction_id),
    INDEX ix_bat_on_demand_spring (spring_batch_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 온디맨드 배치 온라인 접수';

CREATE TABLE IF NOT EXISTS bat_operation_log (
    operation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_operation_log PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_job_time (job_id, created_at),
    INDEX ix_bat_operation_execution (execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 운영 작업 로그';

CREATE TABLE IF NOT EXISTS bat_operation_log_archive (
    operation_id BIGINT NOT NULL COMMENT '원본 배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 등록자',
    created_at DATETIME NOT NULL COMMENT '원본 등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 수정자',
    updated_at DATETIME NOT NULL COMMENT '원본 수정일시',
    archived_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '보관 일시',
    archived_by VARCHAR(100) NOT NULL COMMENT '보관 수행자',
    archive_reason VARCHAR(500) NOT NULL COMMENT '보관 사유',
    CONSTRAINT pk_bat_operation_log_archive PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_archive_job_time (job_id, created_at),
    INDEX ix_bat_operation_archive_archived (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 운영 로그 보관소';

CREATE TABLE IF NOT EXISTS bat_runtime_capability (
    instance_id VARCHAR(160) NOT NULL COMMENT 'Runtime instance identifier',
    capability_code VARCHAR(80) NOT NULL COMMENT 'Advertised capability code',
    CONSTRAINT pk_bat_runtime_capability PRIMARY KEY (instance_id, capability_code),
    CONSTRAINT fk_bat_runtime_capability_instance FOREIGN KEY (instance_id) REFERENCES bat_runtime_instance (instance_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime capability projection';

CREATE TABLE IF NOT EXISTS bat_runtime_command (
    command_id VARCHAR(80) NULL COMMENT 'Runtime command identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Command idempotency key',
    command_type VARCHAR(80) NOT NULL COMMENT 'Approved command type',
    target_type VARCHAR(40) NOT NULL COMMENT 'Command target type',
    target_snapshot LONGTEXT NULL COMMENT 'Command target snapshot JSON',
    target_snapshot_hash VARCHAR(128) NULL COMMENT 'Target snapshot checksum',
    expected_version BIGINT NULL COMMENT 'Expected target version',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Command requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory command reason',
    approval_policy_version VARCHAR(80) NULL COMMENT 'Approval policy version',
    approval_request_id VARCHAR(80) NULL COMMENT 'ADM approval request identifier',
    approved_by VARCHAR(120) NULL COMMENT 'Command approver',
    command_state VARCHAR(40) NOT NULL COMMENT 'Command lifecycle state',
    execution_attempt INT NOT NULL DEFAULT 0 COMMENT 'Execution attempt count',
    result_text LONGTEXT NULL COMMENT 'Command result detail',
    failure_stage VARCHAR(80) NULL COMMENT 'Last failed stage',
    before_state LONGTEXT NULL COMMENT 'State before operation',
    after_state LONGTEXT NULL COMMENT 'State after operation',
    result_code VARCHAR(80) NULL COMMENT 'Command result code',
    requested_at DATETIME(6) NOT NULL COMMENT 'Command request time',
    expires_at DATETIME(6) NULL COMMENT 'Command expiry time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last command state update time',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    evidence_ref VARCHAR(500) NULL COMMENT 'Audit evidence reference',
    CONSTRAINT pk_bat_runtime_command PRIMARY KEY (command_id),
    CONSTRAINT idempotency_key UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT approved runtime command';

CREATE TABLE IF NOT EXISTS bat_runtime_command_attempt (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Command attempt identifier',
    command_id VARCHAR(80) NOT NULL COMMENT 'Runtime command identifier',
    attempt_no INT NOT NULL COMMENT 'Command attempt number',
    instance_id VARCHAR(160) NULL COMMENT 'Target runtime instance identifier',
    stage_code VARCHAR(80) NOT NULL COMMENT 'Attempt execution stage',
    attempt_state VARCHAR(40) NOT NULL COMMENT 'Attempt result state',
    result_message VARCHAR(4000) NULL COMMENT 'Attempt result detail',
    started_at DATETIME(6) NOT NULL COMMENT 'Attempt start time',
    finished_at DATETIME(6) NULL COMMENT 'Attempt finish time',
    CONSTRAINT pk_bat_runtime_command_attempt PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_runtime_command_attempt UNIQUE (command_id, attempt_no, instance_id, stage_code),
    CONSTRAINT fk_bat_runtime_command_attempt_command FOREIGN KEY (command_id) REFERENCES bat_runtime_command (command_id) ON DELETE CASCADE,
    INDEX ix_bat_runtime_command_attempt_instance (instance_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime command execution attempt';

CREATE TABLE IF NOT EXISTS bat_runtime_heartbeat (
    heartbeat_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Heartbeat event identifier',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Runtime instance identifier',
    heartbeat_at DATETIME(6) NOT NULL COMMENT 'Heartbeat observation time',
    ready_yn CHAR(1) NOT NULL COMMENT 'Readiness flag',
    available_capacity INT NOT NULL DEFAULT 0 COMMENT 'Available execution capacity',
    queue_depth BIGINT NOT NULL DEFAULT 0 COMMENT 'Observed queue depth',
    draining_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Drain mode flag',
    current_execution_count INT NOT NULL DEFAULT 0 COMMENT 'Current execution count',
    active_lease_count INT NOT NULL DEFAULT 0 COMMENT 'Active lease count',
    last_error_code VARCHAR(80) NULL COMMENT 'Last runtime error code',
    deployment_version VARCHAR(80) NULL COMMENT 'Observed deployment version',
    CONSTRAINT pk_bat_runtime_heartbeat PRIMARY KEY (heartbeat_id),
    CONSTRAINT fk_bat_runtime_heartbeat_instance FOREIGN KEY (instance_id) REFERENCES bat_runtime_instance (instance_id) ON DELETE CASCADE,
    INDEX ix_bat_runtime_heartbeat_instance (instance_id, heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime heartbeat event';

CREATE TABLE IF NOT EXISTS bat_runtime_instance (
    instance_id VARCHAR(160) NULL COMMENT 'Runtime instance identifier',
    runtime_role VARCHAR(40) NOT NULL COMMENT 'Standalone runtime role',
    service_id VARCHAR(120) NOT NULL COMMENT 'Runtime service identifier',
    was_id VARCHAR(120) NULL COMMENT 'WAS identifier',
    host_alias VARCHAR(160) NULL COMMENT 'Registered host alias',
    zone_id VARCHAR(80) NULL COMMENT 'Availability zone identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Runtime pool identifier',
    artifact_version VARCHAR(80) NOT NULL COMMENT 'Running artifact version',
    git_sha VARCHAR(64) NULL COMMENT 'Running source commit SHA',
    artifact_checksum VARCHAR(128) NULL COMMENT 'Running artifact checksum',
    profile_name VARCHAR(80) NULL COMMENT 'Active runtime profile',
    desired_state VARCHAR(32) NOT NULL DEFAULT 'RUNNING' COMMENT 'Control-plane desired state',
    actual_state VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'Last observed runtime state',
    config_version VARCHAR(80) NULL COMMENT 'Applied configuration version',
    schema_compatibility VARCHAR(120) NULL COMMENT 'Supported schema version range',
    started_at DATETIME(6) NULL COMMENT 'Runtime start time',
    last_heartbeat_at DATETIME(6) NULL COMMENT 'Last heartbeat time',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Monotonic instance fencing token',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Registration time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last state update time',
    CONSTRAINT pk_bat_runtime_instance PRIMARY KEY (instance_id),
    INDEX ix_bat_runtime_instance_service (service_id, actual_state),
    INDEX ix_bat_runtime_instance_heartbeat (last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT standalone runtime instance registry';

CREATE TABLE IF NOT EXISTS bat_schedule (
    schedule_id VARCHAR(100) NOT NULL COMMENT '배치 스케줄 ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron 표현식',
    calendar_id VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '적용 영업일 캘린더 ID',
    business_day_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '영업일에만 수행 여부',
    holiday_policy VARCHAR(30) NOT NULL DEFAULT 'SKIP' COMMENT '휴일 처리 정책',
    available_start_time TIME NULL COMMENT '수행 가능 시작 시각',
    available_end_time TIME NULL COMMENT '수행 가능 종료 시각',
    run_date_pattern VARCHAR(80) NULL COMMENT '수행 일자 패턴',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '스케줄 기준 시간대',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '스케줄 활성 여부',
    last_fire_at DATETIME NULL COMMENT '마지막 실행 예정 일시',
    next_fire_at DATETIME NULL COMMENT '다음 실행 예정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_schedule PRIMARY KEY (schedule_id),
    CONSTRAINT fk_bat_schedule_job FOREIGN KEY (job_id) REFERENCES bat_job (job_id) ON DELETE CASCADE,
    INDEX ix_bat_schedule_job (job_id, enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 스케줄';

CREATE TABLE IF NOT EXISTS bat_schedule_trigger (
    schedule_id VARCHAR(100) NOT NULL COMMENT 'Schedule identifier',
    scheduled_fire_at DATETIME(6) NOT NULL COMMENT 'Planned fire time',
    fencing_token BIGINT NOT NULL COMMENT 'Scheduler fencing token',
    execution_id BIGINT NULL COMMENT 'Created execution identifier',
    trigger_status VARCHAR(30) NOT NULL COMMENT 'Trigger result status',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Trigger record time',
    CONSTRAINT pk_bat_schedule_trigger PRIMARY KEY (schedule_id, scheduled_fire_at),
    CONSTRAINT fk_bat_schedule_trigger_schedule FOREIGN KEY (schedule_id) REFERENCES bat_schedule (schedule_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT scheduled trigger evidence';

CREATE TABLE IF NOT EXISTS bat_scheduler_lease (
    scheduler_key VARCHAR(100) NULL COMMENT 'Scheduler leadership key',
    owner_instance_id VARCHAR(160) NOT NULL COMMENT 'Current leader instance identifier',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic leadership fencing token',
    lease_until DATETIME(6) NOT NULL COMMENT 'Leadership lease expiry time',
    last_heartbeat_at DATETIME(6) NOT NULL COMMENT 'Leader heartbeat time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last lease update time',
    CONSTRAINT pk_bat_scheduler_lease PRIMARY KEY (scheduler_key),
    INDEX ix_bat_scheduler_lease_expire (lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT scheduler leader lease';

CREATE TABLE IF NOT EXISTS bat_step_execution (
    step_execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 Step 실행 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    spring_batch_step_execution_id BIGINT NULL COMMENT 'Spring Batch StepExecution ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    step_name VARCHAR(150) NOT NULL COMMENT 'Step 이름',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT 'Step 메타 마지막 heartbeat 일시',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    step_log MEDIUMTEXT NULL COMMENT 'Step 로그',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_step_execution PRIMARY KEY (step_execution_id),
    CONSTRAINT fk_bat_step_execution_parent FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_step_execution_worker FOREIGN KEY (worker_id) REFERENCES bat_worker (worker_id) ON DELETE SET NULL,
    INDEX ix_bat_step_execution_parent (execution_id, step_name),
    INDEX ix_bat_step_execution_spring (spring_batch_step_execution_id),
    INDEX ix_bat_step_execution_worker (worker_id, start_time),
    INDEX ix_bat_step_execution_heartbeat (execution_status, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Step 실행 이력';

CREATE TABLE IF NOT EXISTS bat_version_compatibility (
    compatibility_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Compatibility rule identifier',
    environment_id VARCHAR(80) NOT NULL DEFAULT '*' COMMENT 'Applicable environment identifier',
    provider_coordinate VARCHAR(200) NOT NULL COMMENT 'Provider artifact coordinate',
    consumer_coordinate VARCHAR(200) NOT NULL DEFAULT '*' COMMENT 'Consumer artifact coordinate',
    min_version VARCHAR(80) NULL COMMENT 'Minimum compatible version',
    max_version VARCHAR(80) NULL COMMENT 'Maximum compatible version',
    schema_range VARCHAR(120) NULL COMMENT 'Compatible schema version range',
    required_capability VARCHAR(80) NULL COMMENT 'Required runtime capability',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Rule enabled flag',
    CONSTRAINT pk_bat_version_compatibility PRIMARY KEY (compatibility_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT artifact and schema compatibility contract';

CREATE TABLE IF NOT EXISTS bat_worker (
    worker_id VARCHAR(160) NOT NULL COMMENT '배치 worker ID',
    server_instance_id VARCHAR(160) NOT NULL COMMENT '서버 인스턴스 ID',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    process_id VARCHAR(80) NULL COMMENT '프로세스 ID',
    thread_name VARCHAR(160) NULL COMMENT '스레드명',
    worker_version VARCHAR(80) NOT NULL DEFAULT 'unknown' COMMENT 'worker 배포 버전',
    capabilities_json LONGTEXT NULL COMMENT 'worker 지원 Job 및 capability JSON',
    max_concurrency INT NOT NULL DEFAULT 1 COMMENT 'worker 최대 동시 실행 수',
    queue_capacity INT NOT NULL DEFAULT 1 COMMENT 'worker 내부 대기열 허용 수',
    control_status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING, DRAINING, STOPPED 제어 상태',
    worker_status VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT 'worker 상태',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    current_job_id VARCHAR(100) NULL COMMENT '현재 실행 Job ID',
    current_execution_id BIGINT NULL COMMENT '현재 BAT 배치 실행 순번',
    description VARCHAR(500) NULL COMMENT 'worker 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_bat_worker PRIMARY KEY (worker_id),
    INDEX ix_bat_worker_server (server_instance_id, active_yn),
    INDEX ix_bat_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_bat_worker_control (control_status, active_yn, last_heartbeat_at),
    INDEX ix_bat_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker heartbeat';

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.
-- Spring Batch 6.0.4 JobRepository sequence contract; do not edit vendor SQL directly.
CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_INSTANCE_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
