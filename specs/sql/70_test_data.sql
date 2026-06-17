-- 로컬 및 통합 검증용 테스트 데이터입니다.

USE pfwDB;

INSERT INTO pfw_file_exchange_log (
    EXCHANGE_ID,
    TRANSACTION_ID,
    TRACE_ID,
    BUSINESS_TRANSACTION_ID,
    ACTION_TYPE,
    PROTOCOL,
    DIRECTION,
    EXECUTED_YN,
    SUCCESS_YN,
    HOST,
    SOURCE_PATH,
    TARGET_PATH,
    REQUEST_USER,
    MESSAGE,
    created_by,
    updated_by
) VALUES (
    'FILE-LOCAL-SAMPLE-001',
    'TEST_TRANSACTION',
    'TEST_TRACE',
    'XYZ08EDU0001',
    'LOCAL_WRITE',
    'LOCAL',
    'WRITE',
    'Y',
    'Y',
    'localhost',
    '/tmp/cpf/source.txt',
    '/tmp/cpf/target.txt',
    'SYSTEM',
    '로컬 파일 교환 샘플 이력입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MESSAGE = VALUES(MESSAGE),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE accDB;

INSERT INTO acc_account (account_id, account_no, account_name, account_status, balance, description, created_by, updated_by)
VALUES
    (1, '100-000-000001', 'ACC 샘플 계정 1', 'ACTIVE', 100000.00, 'ACC 계정 샘플 1', 'SYSTEM', 'SYSTEM'),
    (2, '100-000-000002', 'ACC 샘플 계정 2', 'ACTIVE', 250000.00, 'ACC 계정 샘플 2', 'SYSTEM', 'SYSTEM'),
    (3, '100-000-000003', 'ACC 휴면 계정', 'DORMANT', 0.00, 'ACC 휴면 계정 샘플', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    account_no = VALUES(account_no),
    account_name = VALUES(account_name),
    account_status = VALUES(account_status),
    balance = VALUES(balance),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE mbrDB;

INSERT INTO mbr_member (
    id, member_no, customer_no, login_id, name, email, mobile_no,
    member_status, lock_yn, withdraw_yn, channel_code, description, created_by, updated_by
) VALUES
    (1, 'M000000001', 'C000000001', 'mbr001', '회원 1', 'mbr001@example.com', '010-1000-0001', 'ACTIVE', 'N', 'N', 'WEB', 'MBR 샘플 회원 1', 'SYSTEM', 'SYSTEM'),
    (2, 'M000000002', 'C000000002', 'mbr002', '회원 2', 'mbr002@example.com', '010-1000-0002', 'ACTIVE', 'N', 'N', 'MOBILE', 'MBR 샘플 회원 2', 'SYSTEM', 'SYSTEM'),
    (3, 'M000000003', 'C000000003', 'mbr003', '회원 3', 'mbr003@example.com', '010-1000-0003', 'DORMANT', 'N', 'N', 'WEB', 'MBR 휴면 회원 샘플', 'SYSTEM', 'SYSTEM'),
    (100, 'M000000100', 'C000000100', 'search.target', '검색 대상', 'search@example.com', '010-9999-0100', 'ACTIVE', 'N', 'N', 'WEB', 'MBR 이름 검색 테스트 행', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    customer_no = VALUES(customer_no),
    login_id = VALUES(login_id),
    name = VALUES(name),
    email = VALUES(email),
    mobile_no = VALUES(mobile_no),
    member_status = VALUES(member_status),
    lock_yn = VALUES(lock_yn),
    withdraw_yn = VALUES(withdraw_yn),
    channel_code = VALUES(channel_code),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_role (
    member_id, service_code, role_code, role_name, grade_code, temporary_yn, expire_at,
    granted_by, use_yn, created_by, updated_by
) VALUES
    (1, 'MBR', 'MBR_USER', '일반 회원', 'NORMAL', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, 'MBR', 'MBR_PREMIUM', '프리미엄 회원', 'PREMIUM', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    grade_code = VALUES(grade_code),
    temporary_yn = VALUES(temporary_yn),
    expire_at = VALUES(expire_at),
    granted_by = VALUES(granted_by),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_login_history (
    member_id, login_id, login_result, login_ip, user_agent, failure_reason, created_by, updated_by
)
SELECT 1, 'mbr001', 'SUCCESS', '127.0.0.1', 'SQL-SEED', NULL, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM mbr_member_login_history
    WHERE member_id = 1
      AND login_id = 'mbr001'
      AND user_agent = 'SQL-SEED'
);

USE pfwDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';

INSERT INTO pfw_transaction_log (
    LOG_DATE,
    TRANSACTION_ID,
    TRACE_ID,
    SPAN_ID,
    SEQUENCE_NO,
    MODULE_ID,
    BUSINESS_TRANSACTION_ID,
    BUSINESS_TRANSACTION_NAME,
    LOG_TYPE,
    API_VERSION,
    CLIENT_APP_ID,
    CLIENT_VERSION,
    CALLER_SERVICE,
    CALLER_INSTANCE_ID,
    CORRELATION_ID,
    IDEMPOTENCY_KEY,
    LOCALE,
    TIMEZONE,
    REQUEST_TYPE,
    ORIGINAL_CHANNEL_CODE,
    CHANNEL_CODE,
    MEMBER_NO,
    CUSTOMER_NO,
    SCREEN_ID,
    DEVICE_ID,
    WAS_ID,
    HTTP_METHOD,
    URI,
    CONTROLLER,
    EXECUTION_PACKAGE,
    EXECUTION_CLASS,
    EXECUTION_METHOD,
    EXECUTION_SIGNATURE,
    PARAMETERS,
    REQUEST_BODY,
    RESPONSE,
    HTTP_STATUS,
    RESPONSE_CODE,
    EXEC_USER,
    CLIENT_IP,
    USER_AGENT,
    START_TIME,
    END_TIME,
    DURATION_MS,
    created_by,
    updated_by
)
SELECT
    CURDATE(),
    @sample_transaction_id,
    'trace-sample-001',
    'span-sample-001',
    1,
    'MBR',
    'MBR01BSE0001',
    'MBR 회원 목록 샘플',
    'SUCCESS',
    'v1',
    'cpf-edu-web',
    '1.0.0',
    'xyz-education',
    'local-dev',
    'corr-sample-001',
    'idem-sample-001',
    'ko-KR',
    'Asia/Seoul',
    'NORMAL',
    'WEB',
    'WEB',
    'M000000001',
    'C000000001',
    'MBR_LIST',
    'LOCAL_BROWSER',
    'local01',
    'GET',
    '/mbr/list',
    'cpf.mbr.bse.controller.MbrController',
    'cpf.mbr.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"memberNo":"M000000001","password":"masked"}',
    '{"code":"SPFW000000","message":"정상 처리되었습니다."}',
    200,
    'SPFW000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    NOW(3),
    NOW(3),
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'fixedTelegram', 'M000000001회원1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

USE admDB;

INSERT INTO adm_dynamic_log_level_rule (
    RULE_ID,
    TRANSACTION_ID,
    BUSINESS_TRANSACTION_ID,
    MODULE_ID,
    LOG_LEVEL,
    EXPIRE_AT,
    REASON,
    USE_YN,
    created_by,
    updated_by
) VALUES (
    'sample-rule-001',
    NULL,
    'MBR01BSE0001',
    'MBR',
    'DEBUG',
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    BUSINESS_TRANSACTION_ID = VALUES(BUSINESS_TRANSACTION_ID),
    MODULE_ID = VALUES(MODULE_ID),
    LOG_LEVEL = VALUES(LOG_LEVEL),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    REASON = VALUES(REASON),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
