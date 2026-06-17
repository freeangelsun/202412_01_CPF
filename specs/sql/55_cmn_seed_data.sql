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
