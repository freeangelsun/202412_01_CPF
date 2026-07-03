-- CMN 업무 공통 스키마입니다.
-- 여러 업무 모듈이 함께 사용하는 채번, 알림 로그, 업무 로그를 cmnDB에 배치합니다.

USE cmnDB;

CREATE TABLE IF NOT EXISTS cmn_sequence (
    sequence_key VARCHAR(80) NOT NULL COMMENT '채번 기준 키',
    business_area VARCHAR(50) NOT NULL DEFAULT 'COMMON' COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL DEFAULT 'DEFAULT' COMMENT '업무 키',
    sequence_kind VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '채번 종류',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT '채널 코드',
    prefix VARCHAR(30) NOT NULL COMMENT '채번 접두어',
    date_pattern VARCHAR(20) NULL COMMENT '번호에 포함할 일자 패턴',
    current_value BIGINT NOT NULL DEFAULT 0 COMMENT '현재 채번 값',
    start_value BIGINT NOT NULL DEFAULT 1 COMMENT '초기 시작 번호',
    increment_by INT NOT NULL DEFAULT 1 COMMENT '증가 단위',
    min_value BIGINT NOT NULL DEFAULT 1 COMMENT '허용 최소 번호',
    max_value BIGINT NOT NULL DEFAULT 999999999999999999 COMMENT '허용 최대 번호',
    range_size INT NOT NULL DEFAULT 1 COMMENT '향후 구간 선점 확장을 위한 예약 크기',
    number_length INT NOT NULL DEFAULT 8 COMMENT '번호 숫자 영역 길이',
    reset_cycle VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '초기화 주기',
    reset_pattern VARCHAR(20) NULL COMMENT '초기화 기준 일자 패턴',
    reset_timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '초기화 기준 시간대',
    last_reset_key VARCHAR(20) NULL COMMENT '마지막 초기화 기준 키',
    log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '발급 로그 저장 여부',
    retention_days INT NOT NULL DEFAULT 365 COMMENT '발급 로그 보존 일수',
    description VARCHAR(500) NULL COMMENT '채번 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (sequence_key),
    UNIQUE KEY uk_cmn_sequence_business (business_area, business_key, sequence_kind, channel_code),
    INDEX ix_cmn_sequence_use (use_yn),
    INDEX ix_cmn_sequence_reset (reset_cycle, last_reset_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 채번 기준';

CREATE TABLE IF NOT EXISTS cmn_sequence_issue_log (
    issue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '채번 발급 로그 순번',
    sequence_key VARCHAR(80) NOT NULL COMMENT '채번 기준 키',
    business_area VARCHAR(50) NOT NULL DEFAULT 'COMMON' COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL DEFAULT 'DEFAULT' COMMENT '업무 키',
    sequence_kind VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '채번 종류',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT '채널 코드',
    issued_no VARCHAR(120) NOT NULL COMMENT '최종 발급 번호',
    issued_value BIGINT NOT NULL COMMENT '발급 숫자 값',
    prefix VARCHAR(30) NOT NULL COMMENT '발급 시점 접두어',
    date_key VARCHAR(20) NULL COMMENT '발급 시점 일자 키',
    request_channel VARCHAR(30) NULL COMMENT '요청 채널',
    request_user VARCHAR(100) NULL COMMENT '요청 사용자',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    success_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '발급 성공 여부',
    failure_reason VARCHAR(1000) NULL COMMENT '발급 실패 사유',
    retention_until DATE NULL COMMENT '보존 만료 기준일',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_cmn_sequence_issue_no (issued_no),
    INDEX ix_cmn_sequence_issue_key_time (sequence_key, created_at),
    INDEX ix_cmn_sequence_issue_business_time (business_area, business_key, sequence_kind, channel_code, created_at),
    INDEX ix_cmn_sequence_issue_retention (retention_until),
    CONSTRAINT fk_cmn_sequence_issue_sequence
        FOREIGN KEY (sequence_key) REFERENCES cmn_sequence(sequence_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 채번 발급 로그';

CREATE TABLE IF NOT EXISTS cmn_notification_log (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 로그 순번',
    notification_type VARCHAR(30) NOT NULL COMMENT '알림 유형',
    receiver VARCHAR(200) NOT NULL COMMENT '수신자',
    title VARCHAR(300) NOT NULL COMMENT '알림 제목',
    message TEXT NOT NULL COMMENT '알림 메시지',
    send_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '발송 상태',
    send_result VARCHAR(1000) NULL COMMENT '발송 결과',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (notification_id),
    INDEX ix_cmn_notification_status_time (send_status, created_at),
    INDEX ix_cmn_notification_receiver_time (receiver, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 알림 로그';

CREATE TABLE IF NOT EXISTS cmn_business_log (
    business_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 공통 로그 순번',
    business_area VARCHAR(30) NOT NULL COMMENT '업무 영역',
    business_key VARCHAR(100) NOT NULL COMMENT '업무 키',
    log_type VARCHAR(30) NOT NULL COMMENT '로그 유형',
    log_message VARCHAR(2000) NOT NULL COMMENT '로그 메시지',
    log_payload LONGTEXT NULL COMMENT '로그 상세 payload',
    transaction_id VARCHAR(100) NULL COMMENT '프레임워크 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT '분산 추적 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (business_log_id),
    INDEX ix_cmn_business_log_area_key (business_area, business_key),
    INDEX ix_cmn_business_log_type_time (log_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 공통 업무 로그';
CREATE TABLE IF NOT EXISTS cmn_edu_query_item (
    item_id BIGINT NOT NULL COMMENT '교육 조회 항목 ID',
    item_name VARCHAR(200) NOT NULL COMMENT '교육 조회 항목명',
    category_code VARCHAR(30) NOT NULL COMMENT '교육 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    owner_member_no VARCHAR(50) NULL COMMENT '예시 담당 회원 번호',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (item_id),
    INDEX ix_cmn_edu_query_item_search (status_code, category_code, item_name),
    INDEX ix_cmn_edu_query_item_created (created_at, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN EDU 조회 샘플 항목';

CREATE TABLE IF NOT EXISTS cmn_fixed_length_layout (
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

CREATE TABLE IF NOT EXISTS cmn_fixed_length_group (
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
        FOREIGN KEY (layout_id) REFERENCES cmn_fixed_length_layout(layout_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 반복부 사전';

CREATE TABLE IF NOT EXISTS cmn_fixed_length_field (
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
        FOREIGN KEY (layout_id) REFERENCES cmn_fixed_length_layout(layout_id),
    CONSTRAINT fk_cmn_fixed_length_field_group
        FOREIGN KEY (group_id) REFERENCES cmn_fixed_length_group(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 필드 사전';

CREATE TABLE IF NOT EXISTS cmn_fixed_length_masking_policy (
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
        FOREIGN KEY (layout_id) REFERENCES cmn_fixed_length_layout(layout_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 고정길이 전문 마스킹 정책';
