-- CPF V20 migration.
-- 거래 segment 식별자 분리, EXS 송수신 로그 원장 확장, CMN 고정길이 전문 사전 착수 스키마입니다.

ALTER TABLE pfwDB.pfw_transaction_segment
    ADD COLUMN IF NOT EXISTS user_id_masked VARCHAR(80) NULL COMMENT '마스킹된 사용자 ID' AFTER member_no_masked,
    ADD COLUMN IF NOT EXISTS operator_id_masked VARCHAR(80) NULL COMMENT '마스킹된 운영자 ID' AFTER user_id_masked,
    ADD COLUMN IF NOT EXISTS client_app_id VARCHAR(100) NULL COMMENT '클라이언트 애플리케이션 ID' AFTER original_channel_code,
    ADD COLUMN IF NOT EXISTS caller_service VARCHAR(100) NULL COMMENT '호출 서비스 ID' AFTER client_app_id;

CREATE INDEX IF NOT EXISTS ix_pfw_transaction_segment_user
    ON pfwDB.pfw_transaction_segment (user_id_masked, started_at);
CREATE INDEX IF NOT EXISTS ix_pfw_transaction_segment_operator
    ON pfwDB.pfw_transaction_segment (operator_id_masked, started_at);
CREATE INDEX IF NOT EXISTS ix_pfw_transaction_segment_client
    ON pfwDB.pfw_transaction_segment (client_app_id, caller_service, started_at);

ALTER TABLE exsDB.exs_transaction_log
    ADD COLUMN IF NOT EXISTS transaction_segment_id VARCHAR(120) NULL COMMENT 'CPF 거래 구간 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS api_path VARCHAR(500) NULL COMMENT '요청 API 경로' AFTER endpoint_code,
    ADD COLUMN IF NOT EXISTS request_header_masked MEDIUMTEXT NULL COMMENT '마스킹된 요청 헤더' AFTER request_uri,
    ADD COLUMN IF NOT EXISTS response_header_masked MEDIUMTEXT NULL COMMENT '마스킹된 응답 헤더' AFTER request_header_masked,
    ADD COLUMN IF NOT EXISTS request_payload_masked MEDIUMTEXT NULL COMMENT '마스킹된 요청 payload' AFTER response_header_masked,
    ADD COLUMN IF NOT EXISTS response_payload_masked MEDIUMTEXT NULL COMMENT '마스킹된 응답 payload' AFTER request_payload_masked,
    ADD COLUMN IF NOT EXISTS http_status INT NULL COMMENT 'HTTP 상태 코드' AFTER result_code,
    ADD COLUMN IF NOT EXISTS timeout_ms INT NULL COMMENT 'timeout 밀리초' AFTER retryable_yn,
    ADD COLUMN IF NOT EXISTS retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수' AFTER timeout_ms;

CREATE INDEX IF NOT EXISTS ix_exs_transaction_log_segment
    ON exsDB.exs_transaction_log (transaction_segment_id);
CREATE INDEX IF NOT EXISTS ix_exs_transaction_log_global_segment
    ON exsDB.exs_transaction_log (transaction_global_id, transaction_segment_id);

ALTER TABLE exsDB.exs_message_log
    ADD COLUMN IF NOT EXISTS transaction_segment_id VARCHAR(120) NULL COMMENT 'CPF 거래 구간 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS message_code VARCHAR(80) NULL COMMENT '대외 전문 코드' AFTER direction,
    ADD COLUMN IF NOT EXISTS request_payload_masked MEDIUMTEXT NULL COMMENT '마스킹된 요청 전문' AFTER message_summary,
    ADD COLUMN IF NOT EXISTS response_payload_masked MEDIUMTEXT NULL COMMENT '마스킹된 응답 전문' AFTER request_payload_masked,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PRE_SAVED' COMMENT '전문 처리 상태' AFTER payload_ref,
    ADD COLUMN IF NOT EXISTS failure_code VARCHAR(100) NULL COMMENT '실패 코드' AFTER status,
    ADD COLUMN IF NOT EXISTS failure_message_masked VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지' AFTER failure_code;

CREATE INDEX IF NOT EXISTS ix_exs_message_log_segment
    ON exsDB.exs_message_log (transaction_segment_id, created_at);

CREATE TABLE IF NOT EXISTS cmnDB.cmn_fixed_length_layout (
    layout_id VARCHAR(120) NOT NULL COMMENT '고정길이 전문 layout ID',
    institution_code VARCHAR(50) NOT NULL COMMENT '기관 코드',
    message_code VARCHAR(80) NOT NULL COMMENT '전문 코드',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    version VARCHAR(30) NOT NULL DEFAULT '1.0' COMMENT 'layout 버전',
    charset_name VARCHAR(40) NOT NULL DEFAULT 'UTF-8' COMMENT '문자셋',
    total_length INT NOT NULL COMMENT '전체 전문 길이',
    header_length INT NOT NULL DEFAULT 0 COMMENT '헤더 길이',
    body_length INT NOT NULL DEFAULT 0 COMMENT '본문 길이',
    trailer_length INT NOT NULL DEFAULT 0 COMMENT '트레일러 길이',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    description VARCHAR(500) NULL COMMENT 'layout 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (layout_id),
    UNIQUE KEY uk_cmn_fixed_length_layout (institution_code, message_code, direction, version),
    INDEX ix_cmn_fixed_length_layout_enabled (enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 layout 사전';

CREATE TABLE IF NOT EXISTS cmnDB.cmn_fixed_length_group (
    group_id VARCHAR(120) NOT NULL COMMENT '고정길이 반복부 group ID',
    layout_id VARCHAR(120) NOT NULL COMMENT 'layout ID',
    group_name VARCHAR(100) NOT NULL COMMENT '반복부명',
    display_name VARCHAR(200) NOT NULL COMMENT '반복부 표시명',
    start_position INT NOT NULL COMMENT '1-base 시작 위치',
    repeat_count INT NOT NULL DEFAULT 1 COMMENT '고정 반복 횟수',
    repeat_count_field VARCHAR(100) NULL COMMENT '반복 횟수 기준 필드',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (group_id),
    INDEX ix_cmn_fixed_length_group_layout (layout_id, start_position),
    CONSTRAINT fk_cmn_fixed_length_group_layout
        FOREIGN KEY (layout_id) REFERENCES cmnDB.cmn_fixed_length_layout(layout_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 반복부 사전';

CREATE TABLE IF NOT EXISTS cmnDB.cmn_fixed_length_field (
    field_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '고정길이 필드 순번',
    layout_id VARCHAR(120) NOT NULL COMMENT 'layout ID',
    group_id VARCHAR(120) NULL COMMENT '반복부 group ID',
    field_name VARCHAR(100) NOT NULL COMMENT '필드명',
    display_name VARCHAR(200) NOT NULL COMMENT '필드 표시명',
    start_position INT NOT NULL COMMENT '1-base 시작 위치',
    field_length INT NOT NULL COMMENT '필드 길이',
    byte_length INT NOT NULL COMMENT '필드 byte 길이',
    field_type VARCHAR(30) NOT NULL DEFAULT 'STRING' COMMENT '필드 타입',
    required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '필수 여부',
    padding_char CHAR(1) NOT NULL DEFAULT ' ' COMMENT 'padding 문자',
    align VARCHAR(10) NOT NULL DEFAULT 'LEFT' COMMENT '정렬 방향',
    scale INT NOT NULL DEFAULT 0 COMMENT '숫자 소수 자리',
    format_pattern VARCHAR(80) NULL COMMENT '형식 패턴',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감정보 여부',
    masking_type VARCHAR(30) NULL COMMENT '마스킹 유형',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (field_id),
    UNIQUE KEY uk_cmn_fixed_length_field (layout_id, field_name),
    INDEX ix_cmn_fixed_length_field_layout_pos (layout_id, start_position),
    INDEX ix_cmn_fixed_length_field_group (group_id, start_position),
    CONSTRAINT fk_cmn_fixed_length_field_layout
        FOREIGN KEY (layout_id) REFERENCES cmnDB.cmn_fixed_length_layout(layout_id),
    CONSTRAINT fk_cmn_fixed_length_field_group
        FOREIGN KEY (group_id) REFERENCES cmnDB.cmn_fixed_length_group(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 필드 사전';

CREATE TABLE IF NOT EXISTS cmnDB.cmn_fixed_length_masking_policy (
    masking_policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '고정길이 마스킹 정책 순번',
    layout_id VARCHAR(120) NOT NULL COMMENT 'layout ID',
    field_name VARCHAR(100) NOT NULL COMMENT '마스킹 대상 필드명',
    masking_type VARCHAR(30) NOT NULL COMMENT '마스킹 유형',
    visible_prefix INT NOT NULL DEFAULT 0 COMMENT '앞쪽 표시 길이',
    visible_suffix INT NOT NULL DEFAULT 0 COMMENT '뒤쪽 표시 길이',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (masking_policy_id),
    UNIQUE KEY uk_cmn_fixed_length_masking (layout_id, field_name),
    CONSTRAINT fk_cmn_fixed_length_masking_layout
        FOREIGN KEY (layout_id) REFERENCES cmnDB.cmn_fixed_length_layout(layout_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 마스킹 정책';
