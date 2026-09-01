-- AUTO-GENERATED from CPF canonical schema/profile contracts
-- vendor=mariadb; each logical section executes in its profile-selected physical database.
-- DO NOT EDIT generated verify SQL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
SELECT 'cpfDB.table_count' AS check_name,
       IF(COUNT(*) = 201, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';

SELECT 'cpfDB.table_engine_collation' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
  AND (UPPER(COALESCE(engine, '')) <> 'INNODB'
       OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci');

SELECT 'cpfDB.runtime_transaction_id_contract' AS check_name,
       IF(COUNT(*) = 28 AND COALESCE(SUM(CASE
           WHEN UPPER(table_name) = 'ADM_APPROVAL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_APPROVAL_REQUEST' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_AUDIT_DELIVERY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_AUDIT_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_DYNAMIC_LOG_LEVEL_RULE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_INCIDENT_LIFECYCLE' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'ADM_INCIDENT_SIGNAL' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_ITEM' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_RESULT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_JOB_DEFINITION_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_ON_DEMAND_REQUEST' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_RUNTIME_COMMAND' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_BROKER_DLQ' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_BROKER_OUTBOX' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_FILE_TRANSFER_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_SAGA_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LINEAGE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LINEAGE_ARCHIVE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_SEGMENT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_UNKNOWN_RESULT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'GW_TRANSACTION' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'OPS_ASYNC_OPERATION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'OPS_SERVICE_CALL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'OPS_TRANSACTION_SUBJECT' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 128 THEN 1
           WHEN UPPER(table_name) = 'SEC_TOKEN_AUDIT_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           ELSE 0 END), 0) = 28, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND LOWER(column_name) = 'transaction_id';

SELECT 'cpfDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM CMN_CODE WHERE (code_key = 'CODE_GROUP' AND code_value = 'MODULE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') OR
               (code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'YN') OR
               (code_key = 'MODULE' AND code_value = 'CPF') OR
               (code_key = 'MODULE' AND code_value = 'CMN') OR
               (code_key = 'MODULE' AND code_value = 'ADM') OR
               (code_key = 'MODULE' AND code_value = 'MBW') OR
               (code_key = 'MODULE' AND code_value = 'BAT') OR
               (code_key = 'MODULE' AND code_value = 'EDU') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'NORMAL') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'COMPENSATION') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'RETRY') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'WEB') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'MOBILE') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'BATCH') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'ADM') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'S') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'E') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'FIXED') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'INDEXED') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'TRACE') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'DEBUG') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'INFO') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'WARN') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'ERROR') OR
               (code_key = 'CACHE_NAME' AND code_value = 'ALL') OR
               (code_key = 'CACHE_NAME' AND code_value = 'CODE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'MESSAGE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'RESPONSE_CODE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'CONFIG') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'TASKLET') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'CHUNK') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'RETRY') OR
               (code_key = 'YN' AND code_value = 'Y') OR
               (code_key = 'YN' AND code_value = 'N') OR
               (code_key = 'CODE_GROUP' AND code_value = 'HTTP_METHOD') OR
               (code_key = 'CODE_GROUP' AND code_value = 'EXECUTION_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'ASYNC_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RETRY_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'IDEMPOTENCY_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'HEALTH_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CIRCUIT_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'FILE_SCAN_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'DATA_CLASSIFICATION') OR
               (code_key = 'CODE_GROUP' AND code_value = 'APPROVAL_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'ERROR_CATEGORY') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RETENTION_ACTION') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'GET') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'POST') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'PUT') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'PATCH') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'DELETE') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'READY') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'RUNNING') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'SUCCESS') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'WAITING') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'PROCESSING') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'COMPLETED') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'DLQ') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'RETRYABLE') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'NON_RETRYABLE') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'EXHAUSTED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'PROCESSING') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'COMPLETED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'UP') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'DOWN') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'DEGRADED') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'CLOSED') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'OPEN') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'HALF_OPEN') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'PENDING') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'CLEAN') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'INFECTED') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'QUARANTINED') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'PUBLIC') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'INTERNAL') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'CONFIDENTIAL') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'RESTRICTED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'DRAFT') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'IN_REVIEW') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'APPROVED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'REJECTED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'WITHDRAWN') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'CANCELED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'EXPIRED') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'VALIDATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'AUTHENTICATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'AUTHORIZATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'CONFLICT') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'TIMEOUT') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'TARGET_DOWN') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'ARCHIVE') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'PURGE') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'LEGAL_HOLD') OR
               (code_key = 'CODE_GROUP' AND code_value = 'SORT_DIRECTION') OR
               (code_key = 'SORT_DIRECTION' AND code_value = 'ASC') OR
               (code_key = 'SORT_DIRECTION' AND code_value = 'DESC') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'O') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'S') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'B') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'APP') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'JUT') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'W') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'PARAMETER') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'SPRING_BATCH') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'WORKER') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'SCHEDULER') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'CENTER_CUT')) = 121
           AND (SELECT COUNT(*) FROM CMN_MESSAGE WHERE (message_code = 'MCPF000000' AND locale = 'ko') OR
               (message_code = 'MCPF010001' AND locale = 'ko') OR
               (message_code = 'MCPF010002' AND locale = 'ko') OR
               (message_code = 'MCPF010003' AND locale = 'ko') OR
               (message_code = 'MCPF010004' AND locale = 'ko') OR
               (message_code = 'MCPF010005' AND locale = 'ko') OR
               (message_code = 'MCPF010006' AND locale = 'ko') OR
               (message_code = 'MCPF020001' AND locale = 'ko') OR
               (message_code = 'MCPF030001' AND locale = 'ko') OR
               (message_code = 'MCPF900001' AND locale = 'ko') OR
               (message_code = 'MCPF900002' AND locale = 'ko') OR
               (message_code = 'MCPF900003' AND locale = 'ko') OR
               (message_code = 'MCPF900004' AND locale = 'ko') OR
               (message_code = 'MCPF900005' AND locale = 'ko') OR
               (message_code = 'MCPF990000' AND locale = 'ko') OR
               (message_code = 'MCPF990001' AND locale = 'ko') OR
               (message_code = 'MMBW000000' AND locale = 'ko') OR
               (message_code = 'MMBW010001' AND locale = 'ko') OR
               (message_code = 'MMBW010002' AND locale = 'ko') OR
               (message_code = 'MEDU010001' AND locale = 'ko') OR
               (message_code = 'MCMN000001' AND locale = 'ko') OR
               (message_code = 'MCMN000001' AND locale = 'en') OR
               (message_code = 'MCPF030002' AND locale = 'ko') OR
               (message_code = 'MCPF030003' AND locale = 'ko') OR
               (message_code = 'MCPF030004' AND locale = 'ko') OR
               (message_code = 'MCPF020002' AND locale = 'ko') OR
               (message_code = 'MCPF020003' AND locale = 'ko') OR
               (message_code = 'MCPF040001' AND locale = 'ko') OR
               (message_code = 'MCPF040002' AND locale = 'ko') OR
               (message_code = 'MCPF020004' AND locale = 'ko') OR
               (message_code = 'MCPF020005' AND locale = 'ko') OR
               (message_code = 'MCPF020006' AND locale = 'ko') OR
               (message_code = 'MCPF020007' AND locale = 'ko') OR
               (message_code = 'MCPF040003' AND locale = 'ko') OR
               (message_code = 'MCPF040004' AND locale = 'ko') OR
               (message_code = 'MCPF050001' AND locale = 'ko') OR
               (message_code = 'MCPF050002' AND locale = 'ko')) = 37
           AND (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE (response_code = 'SCPF000000') OR
               (response_code = 'ECPF010001') OR
               (response_code = 'ECPF010002') OR
               (response_code = 'ECPF010003') OR
               (response_code = 'ECPF010004') OR
               (response_code = 'ECPF010005') OR
               (response_code = 'ECPF010006') OR
               (response_code = 'ECPF020001') OR
               (response_code = 'ECPF030001') OR
               (response_code = 'ECPF900001') OR
               (response_code = 'ECPF900002') OR
               (response_code = 'ECPF900003') OR
               (response_code = 'ECPF900004') OR
               (response_code = 'ECPF900005') OR
               (response_code = 'ECPF990000') OR
               (response_code = 'ECPF990001') OR
               (response_code = 'SMBW000000') OR
               (response_code = 'EMBW010001') OR
               (response_code = 'EMBW010002') OR
               (response_code = 'EEDU010001') OR
               (response_code = 'ECPF030002') OR
               (response_code = 'ECPF030003') OR
               (response_code = 'ECPF030004') OR
               (response_code = 'ECPF020002') OR
               (response_code = 'ECPF020003') OR
               (response_code = 'ECPF040001') OR
               (response_code = 'ECPF040002') OR
               (response_code = 'ECPF020004') OR
               (response_code = 'ECPF020005') OR
               (response_code = 'ECPF020006') OR
               (response_code = 'ECPF020007') OR
               (response_code = 'ECPF040003') OR
               (response_code = 'ECPF040004') OR
               (response_code = 'ECPF050001') OR
               (response_code = 'ECPF050002')) = 35
           AND (SELECT COUNT(*) FROM CMN_PARAMETER WHERE (config_key = 'CPF.CMN.CACHE.PRELOAD_ENABLED') OR
               (config_key = 'CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP') OR
               (config_key = 'CPF.CMN.CACHE.REFRESH_POLL_MILLIS') OR
               (config_key = 'CPF.CMN.MESSAGING.BROKER') OR
               (config_key = 'CPF.HTTP.CONNECT_TIMEOUT_MS') OR
               (config_key = 'CPF.HTTP.READ_TIMEOUT_MS') OR
               (config_key = 'CPF.ADM.SESSION_TTL_SECONDS') OR
               (config_key = 'CPF.ADM.PASSWORD_EXPIRE_DAYS') OR
               (config_key = 'CPF.ADM.PASSWORD_MIN_LENGTH') OR
               (config_key = 'CPF.ADM.PASSWORD_MAX_FAIL_COUNT') OR
               (config_key = 'CPF.BATCH.DEFAULT_LOCK_SECONDS') OR
               (config_key = 'CPF.FEATURE.SAMPLE_ENABLED') OR
               (config_key = 'CPF.MBW.SECURITY.MAX_LOGIN_FAIL_COUNT') OR
               (config_key = 'CPF.MBW.SECURITY.ACCESS_TOKEN_TTL_SECONDS') OR
               (config_key = 'CPF.MBW.SECURITY.REFRESH_TOKEN_TTL_SECONDS') OR
               (config_key = 'CPF.RETENTION.EXECUTE_ENABLED') OR
               (config_key = 'CPF.FILE.DOWNLOAD_REQUIRE_CLEAN') OR
               (config_key = 'CPF.HEALTH.INSTANCE_ID_REQUIRED') OR
               (config_key = 'CPF.PAGING.DEFAULT_SIZE') OR
               (config_key = 'CPF.PAGING.MAX_SIZE') OR
               (config_key = 'CPF.RETENTION.DRY_RUN_DEFAULT') OR
               (config_key = 'CPF.SECRET.CACHE_TTL_SECONDS') OR
               (config_key = 'CPF.TENANT.ENABLED') OR
               (config_key = 'CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS')) = 24,
           1, 0
       ) AS passed;

SELECT 'cpfDB.response_code_http_status' AS check_name,
       IF((SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE (response_code = 'SCPF000000') OR
               (response_code = 'ECPF010001') OR
               (response_code = 'ECPF010002') OR
               (response_code = 'ECPF010003') OR
               (response_code = 'ECPF010004') OR
               (response_code = 'ECPF010005') OR
               (response_code = 'ECPF010006') OR
               (response_code = 'ECPF020001') OR
               (response_code = 'ECPF030001') OR
               (response_code = 'ECPF900001') OR
               (response_code = 'ECPF900002') OR
               (response_code = 'ECPF900003') OR
               (response_code = 'ECPF900004') OR
               (response_code = 'ECPF900005') OR
               (response_code = 'ECPF990000') OR
               (response_code = 'ECPF990001') OR
               (response_code = 'SMBW000000') OR
               (response_code = 'EMBW010001') OR
               (response_code = 'EMBW010002') OR
               (response_code = 'EEDU010001') OR
               (response_code = 'ECPF030002') OR
               (response_code = 'ECPF030003') OR
               (response_code = 'ECPF030004') OR
               (response_code = 'ECPF020002') OR
               (response_code = 'ECPF020003') OR
               (response_code = 'ECPF040001') OR
               (response_code = 'ECPF040002') OR
               (response_code = 'ECPF020004') OR
               (response_code = 'ECPF020005') OR
               (response_code = 'ECPF020006') OR
               (response_code = 'ECPF020007') OR
               (response_code = 'ECPF040003') OR
               (response_code = 'ECPF040004') OR
               (response_code = 'ECPF050001') OR
               (response_code = 'ECPF050002')) = 35
          AND NOT EXISTS (SELECT 1 FROM CMN_RESPONSE_CODE WHERE http_status NOT BETWEEN 100 AND 599), 1, 0) AS passed;

SELECT 'cpfDB.admin_product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM ADM_ROLE WHERE USE_YN = 'Y') >= 5
           AND (SELECT COUNT(*) FROM ADM_MENU WHERE USE_YN = 'Y') >= 30
           AND (SELECT COUNT(*) FROM ADM_API_PERMISSION WHERE USE_YN = 'Y') >= 10,
           1, 0
       ) AS passed;

SELECT 'cpfDB.removed_stale_tables_absent' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('cpf_file_exchange_log','adm_operation_log');

SELECT 'cpfDB.adm_operator_account_safety_columns' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND UPPER(table_name) = 'ADM_OPERATOR'
  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');

SELECT 'cpfDB.adm_contact_ownership' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 0
         AND
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR_PROFILE' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 2,
         1, 0
       ) AS passed;

SELECT 'cpfDB.adm_operator_status_constraint' AS check_name,
       IF(COUNT(*) = 1, 1, 0) AS passed
FROM information_schema.table_constraints
WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND constraint_name='ck_adm_operator_status';

-- CPF_LOGICAL_DATABASE=mbwDB
SELECT 'mbwDB.table_count' AS check_name,
       IF(COUNT(*) = 30, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';

SELECT 'mbwDB.table_engine_collation' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
  AND (UPPER(COALESCE(engine, '')) <> 'INNODB'
       OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci');

SELECT 'mbwDB.runtime_transaction_id_contract' AS check_name,
       IF(COUNT(*) = 7 AND COALESCE(SUM(CASE
           WHEN UPPER(table_name) = 'MBW_APPROVAL_DOCUMENT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_APPROVAL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_APPROVAL_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_BUSINESS_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_DOWNLOAD_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_LOGIN_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_REFRESH_TOKEN' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           ELSE 0 END), 0) = 7, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND LOWER(column_name) = 'transaction_id';

SELECT 'mbwDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM MBW_ROLE WHERE use_yn = 'Y') >= 4
           AND (SELECT COUNT(*) FROM MBW_MENU WHERE use_yn = 'Y') >= 8
           AND (SELECT COUNT(*) FROM MBW_PERMISSION WHERE role_code = 'MBW_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8,
           1, 0
       ) AS passed;

SELECT 'mbwDB.removed_stale_tables_absent' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('mbw_customer','mbw_product','mbw_order','mbw_masking_audit');

SELECT 'mbwDB.admin_user_account_safety_columns' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_ADMIN_USER'
  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');

SELECT 'mbwDB.employee_status_default' AS check_name,
       IF(MAX(UPPER(REPLACE(COALESCE(column_default,''), CHAR(39), ''))) = 'EMPLOYED', 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_EMPLOYEE' AND LOWER(column_name)='employment_status';

SELECT 'mbwDB.status_constraints' AS check_name,
       IF(COUNT(*) = 2, 1, 0) AS passed
FROM information_schema.table_constraints
WHERE table_schema=DATABASE() AND ((UPPER(table_name)='MBW_ADMIN_USER' AND constraint_name='ck_mbw_admin_user_status')
   OR (UPPER(table_name)='MBW_EMPLOYEE' AND constraint_name='ck_mbw_employee_status'));

SELECT 'mbwDB.login_operation_contract' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_LOGIN_OPERATION' AND table_type='BASE TABLE') = 1
         AND
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_REFRESH_TOKEN' AND LOWER(column_name)='login_operation_id') = 1,
         1, 0
       ) AS passed;

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- CPF_LOGICAL_DATABASE=cpfDB
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       IF(
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND LEFT(UPPER(table_name), 7) = 'BAT_SB_') = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND UPPER(table_name) IN ('BAT_SB_JOB_INSTANCE_SEQ', 'BAT_SB_JOB_EXECUTION_SEQ', 'BAT_SB_STEP_EXECUTION_SEQ')) = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND UPPER(table_name) IN ('BATCH_JOB_EXECUTION_SEQ', 'BATCH_JOB_INSTANCE_SEQ', 'BATCH_JOB_SEQ', 'BATCH_STEP_EXECUTION_SEQ')) = 0,
           1, 0
       ) AS passed;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
