CREATE DATABASE IF NOT EXISTS refDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE refDB;

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_target (
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NOT NULL COMMENT '업무 기준일',
    target_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    status_code VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태 코드',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    started_at DATETIME NULL COMMENT '처리 시작 일시',
    completed_at DATETIME NULL COMMENT '처리 완료 일시',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    UNIQUE KEY uk_ref_center_cut_sample_target_business (center_cut_job_id, business_key),
    INDEX ix_ref_center_cut_sample_target_status (center_cut_job_id, status_code, business_date),
    INDEX ix_ref_center_cut_sample_target_global (parent_transaction_global_id, child_transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 대상';

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 샘플 결과 순번',
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_ref_center_cut_sample_result_target (target_id),
    INDEX ix_ref_center_cut_sample_result_job (center_cut_job_id, result_status, created_at),
    INDEX ix_ref_center_cut_sample_result_global (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_ref_center_cut_sample_result_target
        FOREIGN KEY (target_id) REFERENCES ref_center_cut_sample_target(target_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 결과';

USE cpfDB;

INSERT INTO cpf_batch_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'TASKLET',
    'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bat_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'refCenterCutTargetProvider',
    'refCenterCutHandler',
    10,
    3,
    'Y',
    'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    batch_job_id = VALUES(batch_job_id),
    center_cut_job_name = VALUES(center_cut_job_name),
    provider_key = VALUES(provider_key),
    handler_key = VALUES(handler_key),
    chunk_size = VALUES(chunk_size),
    retry_limit = VALUES(retry_limit),
    use_yn = VALUES(use_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bat_center_cut_parameter (
    center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'ref_center_cut_sample_target', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'ref_center_cut_sample_result', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE refDB;

INSERT INTO ref_center_cut_sample_target (
    target_id, center_cut_job_id, business_key, business_date, target_payload,
    status_code, retry_count, parent_transaction_global_id, child_transaction_global_id,
    started_at, completed_at, last_error_message, use_yn, created_by, updated_by
) VALUES
    ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    target_payload = VALUES(target_payload),
    status_code = VALUES(status_code),
    retry_count = VALUES(retry_count),
    parent_transaction_global_id = VALUES(parent_transaction_global_id),
    child_transaction_global_id = VALUES(child_transaction_global_id),
    started_at = VALUES(started_at),
    completed_at = VALUES(completed_at),
    last_error_message = VALUES(last_error_message),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
