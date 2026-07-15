-- CMN 업무 공통 기능 초기 데이터입니다.
-- 교육 샘플에서 바로 호출 가능한 채번 기준과 예시 로그를 등록합니다.

USE cmnDB;

INSERT INTO cmn_sequence (
    sequence_key,
    business_area,
    business_key,
    sequence_kind,
    channel_code,
    prefix,
    date_pattern,
    current_value,
    start_value,
    increment_by,
    min_value,
    max_value,
    range_size,
    number_length,
    reset_cycle,
    reset_pattern,
    reset_timezone,
    last_reset_key,
    log_enabled_yn,
    retention_days,
    description,
    use_yn,
    created_by,
    updated_by
) VALUES (
    'CMN_EDU_ORDER',
    'CMN_EDU',
    'ORDER',
    'ORDER_NO',
    'WEB',
    'EDU',
    'yyyyMMdd',
    0,
    1,
    1,
    1,
    999999,
    1,
    6,
    'DAY',
    'yyyyMMdd',
    'Asia/Seoul',
    DATE_FORMAT(CURRENT_DATE, '%Y%m%d'),
    'Y',
    365,
    'CMN 교육용 주문번호 채번 샘플',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    business_area = VALUES(business_area),
    business_key = VALUES(business_key),
    sequence_kind = VALUES(sequence_kind),
    channel_code = VALUES(channel_code),
    prefix = VALUES(prefix),
    date_pattern = VALUES(date_pattern),
    start_value = VALUES(start_value),
    increment_by = VALUES(increment_by),
    min_value = VALUES(min_value),
    max_value = VALUES(max_value),
    range_size = VALUES(range_size),
    number_length = VALUES(number_length),
    reset_cycle = VALUES(reset_cycle),
    reset_pattern = VALUES(reset_pattern),
    reset_timezone = VALUES(reset_timezone),
    log_enabled_yn = VALUES(log_enabled_yn),
    retention_days = VALUES(retention_days),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cmn_notification_log (
    notification_type,
    receiver,
    title,
    message,
    send_status,
    send_result,
    transaction_id,
    trace_id,
    created_by,
    updated_by
)
SELECT
    'EMAIL',
    'developer@example.com',
    'CMN 알림 로그 샘플',
    'CMN 공통 알림 로그 테이블 연동을 확인하기 위한 초기 데이터입니다.',
    'READY',
    NULL,
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cmn_notification_log
    WHERE transaction_id = 'INITIAL_SQL_SEED'
      AND title = 'CMN 알림 로그 샘플'
);

INSERT INTO cmn_business_log (
    business_area,
    business_key,
    log_type,
    log_message,
    log_payload,
    transaction_id,
    trace_id,
    created_by,
    updated_by
)
SELECT
    'CMN_EDU',
    'INITIAL',
    'SEED',
    'CMN 공통 업무 로그 테이블 연동을 확인하기 위한 초기 데이터입니다.',
    '{"source":"55_cmn_seed_data.sql"}',
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cmn_business_log
    WHERE business_area = 'CMN_EDU'
      AND business_key = 'INITIAL'
      AND log_type = 'SEED'
);

INSERT INTO cmn_fixed_length_layout (
    layout_id,
    institution_code,
    message_code,
    direction,
    version,
    charset_name,
    total_length,
    header_length,
    body_length,
    trailer_length,
    enabled_yn,
    description,
    created_by,
    updated_by
) VALUES (
    'BANK01_BALANCE_REQ_V1',
    'BANK01',
    'BALANCE_REQ',
    'OUTBOUND',
    '1.0',
    'UTF-8',
    80,
    20,
    60,
    0,
    'Y',
    '외부기관 잔액조회 요청 교육용 고정길이 전문 layout',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    charset_name = VALUES(charset_name),
    total_length = VALUES(total_length),
    header_length = VALUES(header_length),
    body_length = VALUES(body_length),
    trailer_length = VALUES(trailer_length),
    enabled_yn = VALUES(enabled_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cmn_fixed_length_group (
    group_id,
    layout_id,
    group_name,
    display_name,
    start_position,
    repeat_count,
    repeat_count_field,
    enabled_yn,
    created_by,
    updated_by
) VALUES (
    'BANK01_BALANCE_REQ_BODY',
    'BANK01_BALANCE_REQ_V1',
    'bodyItems',
    '잔액조회 반복부',
    21,
    2,
    NULL,
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    start_position = VALUES(start_position),
    repeat_count = VALUES(repeat_count),
    repeat_count_field = VALUES(repeat_count_field),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cmn_fixed_length_field (
    layout_id,
    group_id,
    field_name,
    display_name,
    start_position,
    field_length,
    byte_length,
    field_type,
    required_yn,
    padding_char,
    align,
    scale,
    format_pattern,
    sensitive_yn,
    masking_type,
    enabled_yn,
    created_by,
    updated_by
) VALUES
('BANK01_BALANCE_REQ_V1', NULL, 'messageCode', '전문 코드', 1, 10, 10, 'STRING', 'Y', ' ', 'LEFT', 0, NULL, 'N', NULL, 'Y', 'SYSTEM', 'SYSTEM'),
('BANK01_BALANCE_REQ_V1', NULL, 'transactionDate', '거래 일자', 11, 8, 8, 'STRING', 'Y', '0', 'RIGHT', 0, 'yyyyMMdd', 'N', NULL, 'Y', 'SYSTEM', 'SYSTEM'),
('BANK01_BALANCE_REQ_V1', NULL, 'itemCount', '반복 건수', 19, 2, 2, 'NUMBER', 'Y', '0', 'RIGHT', 0, NULL, 'N', NULL, 'Y', 'SYSTEM', 'SYSTEM'),
('BANK01_BALANCE_REQ_V1', 'BANK01_BALANCE_REQ_BODY', 'accountNo', '계좌번호', 21, 20, 20, 'STRING', 'Y', ' ', 'LEFT', 0, NULL, 'Y', 'ACCOUNT', 'Y', 'SYSTEM', 'SYSTEM'),
('BANK01_BALANCE_REQ_V1', 'BANK01_BALANCE_REQ_BODY', 'amount', '금액', 41, 10, 10, 'NUMBER', 'Y', '0', 'RIGHT', 0, NULL, 'N', NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    group_id = VALUES(group_id),
    display_name = VALUES(display_name),
    start_position = VALUES(start_position),
    field_length = VALUES(field_length),
    byte_length = VALUES(byte_length),
    field_type = VALUES(field_type),
    required_yn = VALUES(required_yn),
    padding_char = VALUES(padding_char),
    align = VALUES(align),
    scale = VALUES(scale),
    format_pattern = VALUES(format_pattern),
    sensitive_yn = VALUES(sensitive_yn),
    masking_type = VALUES(masking_type),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cmn_fixed_length_masking_policy (
    layout_id,
    field_name,
    masking_type,
    visible_prefix,
    visible_suffix,
    enabled_yn,
    created_by,
    updated_by
) VALUES (
    'BANK01_BALANCE_REQ_V1',
    'accountNo',
    'ACCOUNT',
    3,
    3,
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    masking_type = VALUES(masking_type),
    visible_prefix = VALUES(visible_prefix),
    visible_suffix = VALUES(visible_suffix),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
