-- Local and integration test data for ACC, MBR, PFW, and ADM screens.

USE pfwDB;

INSERT INTO file_exchange_log (
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
    CREATED_BY,
    UPDATED_BY
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
    '/tmp/fps/source.txt',
    '/tmp/fps/target.txt',
    'SYSTEM',
    'Local file exchange sample history.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MESSAGE = VALUES(MESSAGE),
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;

USE accDB;

INSERT INTO acc_account (account_id, account_no, account_name, account_status, balance, description, created_by, updated_by)
VALUES
    (1, '100-000-000001', 'ACC sample account 1', 'ACTIVE', 100000.00, 'ACC account sample 1', 'SYSTEM', 'SYSTEM'),
    (2, '100-000-000002', 'ACC sample account 2', 'ACTIVE', 250000.00, 'ACC account sample 2', 'SYSTEM', 'SYSTEM'),
    (3, '100-000-000003', 'ACC dormant account', 'DORMANT', 0.00, 'ACC dormant account sample', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    account_no = VALUES(account_no),
    account_name = VALUES(account_name),
    account_status = VALUES(account_status),
    balance = VALUES(balance),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE mbrDB;

INSERT INTO member (id, name, description, created_by, updated_by)
VALUES
    (1, 'mbr 1', 'MBR separated DB sample member 1', 'SYSTEM', 'SYSTEM'),
    (2, 'mbr 2', 'MBR separated DB sample member 2', 'SYSTEM', 'SYSTEM'),
    (3, 'mbr 3', 'MBR separated DB sample member 3', 'SYSTEM', 'SYSTEM'),
    (100, 'search target', 'MBR separated DB name search test row', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE pfwDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';

INSERT INTO TRAN_LOG (
    LOG_DATE,
    TRANSACTION_ID,
    TRACE_ID,
    SPAN_ID,
    SEQUENCE_NO,
    MODULE_ID,
    BUSINESS_TRANSACTION_ID,
    BUSINESS_TRANSACTION_NAME,
    LOG_TYPE,
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
    RESPONSE,
    RESPONSE_CODE,
    EXEC_USER,
    CLIENT_IP,
    USER_AGENT,
    START_TIME,
    END_TIME,
    DURATION_MS,
    CREATED_BY,
    UPDATED_BY
)
SELECT
    CURDATE(),
    @sample_transaction_id,
    'trace-sample-001',
    'span-sample-001',
    1,
    'MBR',
    'MBR01BSE0001',
    'MBR member list sample',
    'SUCCESS',
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
    'fps.mbr.bse.controller.MbrController',
    'fps.mbr.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"code":"1000","message":"success"}',
    200,
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
    FROM TRAN_LOG
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM TRAN_LOG
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO TRAN_LOG_DTL (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    CREATED_BY,
    UPDATED_BY
)
SELECT @sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL"}', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM TRAN_LOG_DTL
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO TRAN_LOG_DTL (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    CREATED_BY,
    UPDATED_BY
)
SELECT @sample_log_idx, 'memo', 'Seed transaction log for ADM log screen smoke test.', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM TRAN_LOG_DTL
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

USE admDB;

INSERT INTO dynamic_log_level_rule (
    RULE_ID,
    TRANSACTION_ID,
    BUSINESS_TRANSACTION_ID,
    MODULE_ID,
    LOG_LEVEL,
    EXPIRE_AT,
    REASON,
    USE_YN,
    CREATED_BY,
    UPDATED_BY
) VALUES (
    'sample-rule-001',
    NULL,
    'MBR01BSE0001',
    'MBR',
    'DEBUG',
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'Initial sample dynamic log rule for ADM screen smoke test.',
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
    UPDATED_BY = VALUES(UPDATED_BY),
    UPDATED_AT = CURRENT_TIMESTAMP;
