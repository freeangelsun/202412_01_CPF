-- V4: Spring Batch 표준 저장소와 CPF 배치 운영 보강 migration입니다.
-- 기존 DB에 BATCH_* JobRepository 테이블, 영업일 스케줄 속성, 배치 관계/대상, 운영 알림 기준을 추가합니다.

USE cpfDB;

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

CREATE TABLE IF NOT EXISTS BATCH_STEP_EXECUTION_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch StepExecution 채번 테이블';

CREATE TABLE IF NOT EXISTS BATCH_JOB_EXECUTION_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch JobExecution 채번 테이블';

CREATE TABLE IF NOT EXISTS BATCH_JOB_SEQ (
    ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 채번 값'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch JobInstance 채번 테이블';

INSERT INTO BATCH_JOB_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_SEQ);

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_EXECUTION_SEQ);

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_STEP_EXECUTION_SEQ);

ALTER TABLE cpf_batch_schedule
    ADD COLUMN IF NOT EXISTS calendar_id VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '적용 영업일 캘린더 ID' AFTER cron_expression,
    ADD COLUMN IF NOT EXISTS business_day_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '영업일에만 수행 여부' AFTER calendar_id,
    ADD COLUMN IF NOT EXISTS holiday_policy VARCHAR(30) NOT NULL DEFAULT 'SKIP' COMMENT '휴일 처리 정책' AFTER business_day_only_yn,
    ADD COLUMN IF NOT EXISTS available_start_time TIME NULL COMMENT '수행 가능 시작 시각' AFTER holiday_policy,
    ADD COLUMN IF NOT EXISTS available_end_time TIME NULL COMMENT '수행 가능 종료 시각' AFTER available_start_time,
    ADD COLUMN IF NOT EXISTS run_date_pattern VARCHAR(80) NULL COMMENT '수행 일자 패턴' AFTER available_end_time;

CREATE TABLE IF NOT EXISTS cpf_batch_job_relation (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 관계 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '기준 배치 Job ID',
    related_job_id VARCHAR(100) NOT NULL COMMENT '연관 배치 Job ID',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 유형',
    trigger_condition VARCHAR(50) NOT NULL DEFAULT 'COMPLETED' COMMENT '트리거 조건',
    required_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT '필수 선행 상태',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '관계 표시 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (relation_id),
    UNIQUE KEY uk_cpf_batch_job_relation (job_id, related_job_id, relation_type),
    INDEX ix_cpf_batch_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_cpf_batch_job_relation_related (related_job_id, relation_type),
    CONSTRAINT fk_cpf_batch_job_relation_job
        FOREIGN KEY (job_id) REFERENCES cpf_batch_job(job_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cpf_batch_job_relation_related
        FOREIGN KEY (related_job_id) REFERENCES cpf_batch_job(job_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 배치 선행/후행/트리거 관계';

CREATE TABLE IF NOT EXISTS cpf_batch_execution_target (
    target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 수행 대상 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    target_instance_id VARCHAR(100) NULL COMMENT '수행 대상 인스턴스 ID',
    business_date DATE NULL COMMENT '업무 기준일',
    planned_run_at DATETIME(3) NULL COMMENT '예정 수행 일시',
    dispatch_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '배정 상태',
    dispatch_reason VARCHAR(500) NULL COMMENT '배정 또는 제외 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    INDEX ix_cpf_batch_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_cpf_batch_execution_target_execution (execution_id),
    INDEX ix_cpf_batch_execution_target_instance (target_instance_id, dispatch_status),
    CONSTRAINT fk_cpf_batch_execution_target_execution
        FOREIGN KEY (execution_id) REFERENCES cpf_batch_execution(execution_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cpf_batch_execution_target_job
        FOREIGN KEY (job_id) REFERENCES cpf_batch_job(job_id),
    CONSTRAINT fk_cpf_batch_execution_target_schedule
        FOREIGN KEY (schedule_id) REFERENCES cpf_batch_schedule(schedule_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_cpf_batch_execution_target_instance
        FOREIGN KEY (target_instance_id) REFERENCES cpf_batch_instance(instance_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 배치 수행 대상/대기 인스턴스';

CREATE TABLE IF NOT EXISTS cpf_notification_rule (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    event_sub_type VARCHAR(80) NULL COMMENT '알림 이벤트 세부 유형',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ADM' COMMENT '알림 채널 코드',
    template_code VARCHAR(80) NULL COMMENT '알림 템플릿 코드',
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '알림 심각도',
    receiver_group VARCHAR(100) NULL COMMENT '수신자 그룹',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_cpf_notification_rule (event_type, event_sub_type, channel_code),
    INDEX ix_cpf_notification_rule_use (use_yn, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 규칙';

CREATE TABLE IF NOT EXISTS cpf_notification_delivery_log (
    delivery_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 발송 로그 순번',
    rule_id BIGINT NULL COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    target_type VARCHAR(80) NULL COMMENT '알림 대상 유형',
    target_id VARCHAR(120) NULL COMMENT '알림 대상 ID',
    receiver VARCHAR(200) NULL COMMENT '수신자',
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '발송 상태',
    delivery_message VARCHAR(2000) NULL COMMENT '발송 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발송 요청 일시',
    delivered_at DATETIME(3) NULL COMMENT '발송 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (delivery_id),
    INDEX ix_cpf_notification_delivery_target (target_type, target_id, requested_at),
    INDEX ix_cpf_notification_delivery_status (delivery_status, requested_at),
    CONSTRAINT fk_cpf_notification_delivery_rule
        FOREIGN KEY (rule_id) REFERENCES cpf_notification_rule(rule_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 발송 로그';
