-- BAT가 소유하는 Spring Batch 메타와 배치 런타임 스키마입니다.
-- 표준 Spring Batch 테이블은 BATCH_* 이름을 유지하고 BAT 전용 런타임 테이블은 bat_* 이름을 사용합니다.

USE batDB;

CREATE TABLE IF NOT EXISTS bat_on_demand_request (
    execution_request_id VARCHAR(36) NOT NULL COMMENT '온라인 접수 실행 요청 ID',
    standard_batch_id CHAR(10) NOT NULL COMMENT 'B 유형 10자리 표준 배치 ID',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 접수 방지 멱등 키',
    transaction_global_id VARCHAR(100) NOT NULL COMMENT '온라인 접수 거래 글로벌 ID',
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
    PRIMARY KEY (execution_request_id),
    UNIQUE KEY uk_bat_on_demand_idempotency (standard_batch_id, idempotency_key),
    INDEX ix_bat_on_demand_status (request_status, requested_at),
    INDEX ix_bat_on_demand_transaction (transaction_global_id),
    INDEX ix_bat_on_demand_spring (spring_batch_execution_id),
    CONSTRAINT ck_bat_on_demand_id CHECK (
        standard_batch_id REGEXP '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
        AND RIGHT(standard_batch_id, 4) <> '0000'
    ),
    CONSTRAINT ck_bat_on_demand_status CHECK (
        request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED',
                           'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 온디맨드 배치 온라인 접수';

CREATE TABLE IF NOT EXISTS BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_NAME VARCHAR(100) NOT NULL COMMENT 'Spring Batch Job 이름',
    JOB_KEY VARCHAR(32) NOT NULL COMMENT 'Job 파라미터 식별 키',
    PRIMARY KEY (JOB_INSTANCE_ID),
    UNIQUE KEY JOB_INST_UN (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobInstance 저장소';

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
    PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_INST_EXEC_FK
        FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    PARAMETER_NAME VARCHAR(100) NOT NULL COMMENT '파라미터 이름',
    PARAMETER_TYPE VARCHAR(100) NOT NULL COMMENT '파라미터 Java 유형',
    PARAMETER_VALUE VARCHAR(2500) NULL COMMENT '파라미터 값',
    IDENTIFYING CHAR(1) NOT NULL COMMENT 'JobInstance 식별 파라미터 여부',
    CONSTRAINT JOB_EXEC_PARAMS_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 파라미터 저장소';

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
    PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_STEP_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 StepExecution 저장소';

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT STEP_EXEC_CTX_FK
        FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Step 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_CTX_FK
        FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 컨텍스트 저장소';

-- Spring Batch 5.2.4의 공식 MariaDB JobRepository 계약은 채번용 TABLE이 아니라
-- MariaDB SEQUENCE를 사용합니다. IF NOT EXISTS로 재설치 시 현재 next value를 보존합니다.
CREATE SEQUENCE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BATCH_JOB_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

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
    PRIMARY KEY (job_id),
    INDEX ix_bat_job_use (use_yn, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Job 기준';

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
    PRIMARY KEY (schedule_id),
    INDEX ix_bat_schedule_job (job_id, enabled_yn),
    CONSTRAINT fk_bat_schedule_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 스케줄';

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
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_bat_job_relation (job_id, related_job_id, relation_type),
    INDEX ix_bat_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_bat_job_relation_related (related_job_id, relation_type),
    CONSTRAINT fk_bat_job_relation_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_job_relation_related
        FOREIGN KEY (related_job_id) REFERENCES bat_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 선행/후행/트리거 관계';

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
    PRIMARY KEY (instance_id),
    INDEX ix_bat_instance_active (active_yn, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 서버 인스턴스';

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
    PRIMARY KEY (worker_id),
    INDEX ix_bat_worker_server (server_instance_id, active_yn),
    INDEX ix_bat_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_bat_worker_control (control_status, active_yn, last_heartbeat_at),
    INDEX ix_bat_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker heartbeat';

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
    transaction_global_id VARCHAR(100) NULL COMMENT '전역 거래 ID',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '온라인 호출 또는 상위 배치 거래 ID',
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
    PRIMARY KEY (execution_id),
    INDEX ix_bat_execution_job_time (job_id, start_time),
    INDEX ix_bat_execution_status (execution_status, start_time),
    INDEX ix_bat_execution_spring (spring_batch_execution_id),
    INDEX ix_bat_execution_job_instance (spring_batch_job_instance_id, business_date),
    INDEX ix_bat_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_bat_execution_claim (execution_status, required_worker_version, required_capability, execution_id),
    INDEX ix_bat_execution_transaction (transaction_global_id),
    INDEX ix_bat_execution_parent_transaction (parent_transaction_global_id),
    INDEX ix_bat_execution_segment (transaction_segment_id, parent_segment_id),
    INDEX ix_bat_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_bat_execution_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_execution_instance
        FOREIGN KEY (batch_instance_id) REFERENCES bat_instance(instance_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
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
    released_at DATETIME(3) NULL COMMENT '정상 또는 실패 완료 일시',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실행 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (lease_id),
    UNIQUE KEY uk_bat_execution_lease_execution (execution_id),
    UNIQUE KEY uk_bat_execution_lease_token (lease_token),
    INDEX ix_bat_execution_lease_owner (worker_id, lease_status, lease_until),
    INDEX ix_bat_execution_lease_expire (lease_status, lease_until),
    CONSTRAINT fk_bat_execution_lease_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_execution_lease_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE RESTRICT
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
    PRIMARY KEY (target_id),
    INDEX ix_bat_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_bat_execution_target_execution (execution_id),
    INDEX ix_bat_execution_target_instance (target_instance_id, dispatch_status),
    CONSTRAINT fk_bat_execution_target_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_execution_target_schedule
        FOREIGN KEY (schedule_id) REFERENCES bat_schedule(schedule_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_instance
        FOREIGN KEY (target_instance_id) REFERENCES bat_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 수행 대상/대기 인스턴스';

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
    PRIMARY KEY (step_execution_id),
    INDEX ix_bat_step_execution_parent (execution_id, step_name),
    INDEX ix_bat_step_execution_spring (spring_batch_step_execution_id),
    INDEX ix_bat_step_execution_worker (worker_id, start_time),
    INDEX ix_bat_step_execution_heartbeat (execution_status, last_heartbeat_at),
    CONSTRAINT fk_bat_step_execution_parent
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_step_execution_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Step 실행 이력';

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
    PRIMARY KEY (lock_key),
    INDEX ix_bat_lock_job (job_id, job_parameters_hash),
    INDEX ix_bat_lock_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 중복 실행 방지 잠금';

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
    PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_job_time (job_id, created_at),
    INDEX ix_bat_operation_execution (execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 운영 작업 로그';

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
    PRIMARY KEY (ghost_event_id),
    INDEX ix_bat_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_bat_ghost_event_job (job_id, detected_at),
    INDEX ix_bat_ghost_event_worker (worker_id, detected_at),
    CONSTRAINT fk_bat_ghost_event_execution
        FOREIGN KEY (execution_id) REFERENCES bat_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_bat_ghost_event_job
        FOREIGN KEY (job_id) REFERENCES bat_job(job_id),
    CONSTRAINT fk_bat_ghost_event_worker
        FOREIGN KEY (worker_id) REFERENCES bat_worker(worker_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 ghost 감지와 조치 이력';


CREATE TABLE IF NOT EXISTS bat_business_day_calendar (
    calendar_id VARCHAR(50) NOT NULL COMMENT '캘린더 ID',
    business_date DATE NOT NULL COMMENT '기준 일자',
    holiday_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '휴일 여부',
    business_day_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '영업일 여부',
    description VARCHAR(500) NULL COMMENT '일자 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (calendar_id, business_date),
    INDEX ix_bat_business_day_calendar_date (business_date, business_day_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 영업일 캘린더';

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
    PRIMARY KEY (center_cut_job_id),
    INDEX ix_bat_center_cut_job_batch (batch_job_id, use_yn),
    CONSTRAINT fk_bat_center_cut_job_batch
        FOREIGN KEY (batch_job_id) REFERENCES bat_job(job_id)
        ON DELETE SET NULL
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
    PRIMARY KEY (parameter_id),
    UNIQUE KEY uk_bat_center_cut_parameter (center_cut_job_id, parameter_key),
    CONSTRAINT fk_bat_center_cut_parameter_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 파라미터';

CREATE TABLE IF NOT EXISTS bat_center_cut_item (
    center_cut_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NULL COMMENT '업무 기준일',
    item_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    item_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    started_at DATETIME(3) NULL COMMENT '처리 시작 일시',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_item_id),
    UNIQUE KEY uk_bat_center_cut_item_business (center_cut_job_id, business_key),
    INDEX ix_bat_center_cut_item_status (center_cut_job_id, item_status, business_date),
    INDEX ix_bat_center_cut_item_transaction (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_bat_center_cut_item_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 대상';

CREATE TABLE IF NOT EXISTS bat_center_cut_result (
    center_cut_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 결과 순번',
    center_cut_item_id BIGINT NOT NULL COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (center_cut_result_id),
    INDEX ix_bat_center_cut_result_item (center_cut_item_id, result_status),
    INDEX ix_bat_center_cut_result_job (center_cut_job_id, created_at),
    CONSTRAINT fk_bat_center_cut_result_item
        FOREIGN KEY (center_cut_item_id) REFERENCES bat_center_cut_item(center_cut_item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_result_job
        FOREIGN KEY (center_cut_job_id) REFERENCES bat_center_cut_job(center_cut_job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 결과';
