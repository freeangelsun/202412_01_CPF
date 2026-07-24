USE cpfDB;

-- V16에서 CPF에 임시 배치했던 center-cut 실행 메타를 BAT 소유 테이블로 이관합니다.
-- CPF는 표준 인터페이스와 상태 기준을 제공하고, BAT는 기본 실행 메타와 sample 구현체를 소유합니다.

CREATE TABLE IF NOT EXISTS bat_center_cut_job (
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    batch_job_id VARCHAR(100) NULL COMMENT '연결된 CPF 배치 Job ID',
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
        FOREIGN KEY (batch_job_id) REFERENCES cpf_batch_job(job_id)
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

INSERT INTO bat_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, created_at, updated_by, updated_at
)
SELECT
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, created_at, updated_by, updated_at
FROM cpf_center_cut_job
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
    updated_at = VALUES(updated_at);

INSERT INTO bat_center_cut_parameter (
    parameter_id, center_cut_job_id, parameter_key, parameter_value, encrypted_yn,
    use_yn, created_by, created_at, updated_by, updated_at
)
SELECT
    parameter_id, center_cut_job_id, parameter_key, parameter_value, encrypted_yn,
    use_yn, created_by, created_at, updated_by, updated_at
FROM cpf_center_cut_parameter
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at);

INSERT INTO bat_center_cut_item (
    center_cut_item_id, center_cut_job_id, business_key, business_date, item_status,
    parent_transaction_global_id, child_transaction_global_id, item_payload, retry_count,
    last_error_message, created_by, created_at, updated_by, updated_at
)
SELECT
    center_cut_item_id, center_cut_job_id, business_key, business_date, item_status,
    parent_transaction_global_id, child_transaction_global_id, item_payload, retry_count,
    last_error_message, created_by, created_at, updated_by, updated_at
FROM cpf_center_cut_item
ON DUPLICATE KEY UPDATE
    item_status = VALUES(item_status),
    child_transaction_global_id = VALUES(child_transaction_global_id),
    item_payload = VALUES(item_payload),
    retry_count = VALUES(retry_count),
    last_error_message = VALUES(last_error_message),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at);

INSERT INTO bat_center_cut_result (
    center_cut_result_id, center_cut_item_id, center_cut_job_id, result_status,
    result_payload, result_message, child_transaction_global_id,
    created_by, created_at, updated_by, updated_at
)
SELECT
    center_cut_result_id, center_cut_item_id, center_cut_job_id, result_status,
    result_payload, result_message, child_transaction_global_id,
    created_by, created_at, updated_by, updated_at
FROM cpf_center_cut_result
ON DUPLICATE KEY UPDATE
    result_status = VALUES(result_status),
    result_payload = VALUES(result_payload),
    result_message = VALUES(result_message),
    child_transaction_global_id = VALUES(child_transaction_global_id),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at);

DROP TABLE IF EXISTS cpf_center_cut_result;
DROP TABLE IF EXISTS cpf_center_cut_item;
DROP TABLE IF EXISTS cpf_center_cut_parameter;
DROP TABLE IF EXISTS cpf_center_cut_job;
