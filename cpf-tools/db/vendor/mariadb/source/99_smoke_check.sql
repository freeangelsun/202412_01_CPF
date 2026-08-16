-- cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql
-- ============================================================================
-- CPF MariaDB 공식 설치 검증 계약입니다.
-- Provision -> Empty Install -> Product Seed -> Baseline Registry 이후 실행합니다.
--
-- 모든 SELECT는 반드시 check_name, passed 두 열만 반환합니다.
-- cpf-tools/db/tools/initialize-cpf-database.ps1은 한 건이라도 passed <> 1이면
-- 설치를 실패 처리합니다. Optional Sample/Test Seed 데이터는 요구하지 않습니다.

SELECT 'platform.schema_count' AS check_name,
       IF(COUNT(*) = 6, 1, 0) AS passed
FROM information_schema.schemata
WHERE LOWER(schema_name) IN (
    'cpfdb', 'cmndb', 'admdb', 'bzadb',
    'batdb', 'refdb'
);

SELECT 'platform.schema_charset_collation' AS check_name,
       IF(COUNT(*) = 6, 1, 0) AS passed
FROM information_schema.schemata
WHERE LOWER(schema_name) IN (
    'cpfdb', 'cmndb', 'admdb', 'bzadb',
    'batdb', 'refdb'
)
  AND LOWER(default_character_set_name) = 'utf8mb4'
  AND LOWER(default_collation_name) = 'utf8mb4_unicode_ci';

SELECT 'cpfDB.table_count' AS check_name,
       IF(COUNT(*) = 53, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'cpfdb' AND table_type = 'BASE TABLE';

SELECT 'cmnDB.table_count' AS check_name,
       IF(COUNT(*) = 2, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'cmndb' AND table_type = 'BASE TABLE';

SELECT 'admDB.table_count' AS check_name,
       IF(COUNT(*) = 31, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'admdb' AND table_type = 'BASE TABLE';

SELECT 'bzaDB.table_count' AS check_name,
       IF(COUNT(*) = 28, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'bzadb' AND table_type = 'BASE TABLE';

SELECT 'batDB.table_count' AS check_name,
       IF(COUNT(*) = 43, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'batdb' AND table_type = 'BASE TABLE';

SELECT 'refDB.table_count' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'refdb' AND table_type = 'BASE TABLE';

SELECT 'platform.table_engine_collation' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) IN (
    'cpfdb', 'cmndb', 'admdb', 'bzadb',
    'batdb', 'refdb'
)
  AND table_type = 'BASE TABLE'
  AND (
      UPPER(COALESCE(engine, '')) <> 'INNODB'
      OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci'
  );

SELECT 'batDB.spring_batch_sequence_count' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.tables
WHERE LOWER(table_schema) = 'batdb' AND table_type = 'SEQUENCE';

SELECT 'cpfDB.platform_baseline_registry' AS check_name,
       IF(COUNT(*) = 6 AND COUNT(DISTINCT schema_name) = 6, 1, 0) AS passed
FROM cpfDB.cpf_schema_installation
WHERE database_vendor = 'MARIADB'
  AND product_version = '1.0.0-SNAPSHOT'
  AND baseline_key = 'CPF_PROFILE_INSTALL_V1'
  AND install_state = 'PRODUCT_SEEDED'
  AND LOWER(schema_name) IN (
      'cpfdb', 'cmndb', 'admdb', 'bzadb',
      'batdb', 'refdb'
  );

SELECT 'cpfDB.platform_baseline_identity' AS check_name,
       IF(COUNT(*) = 6, 1, 0) AS passed
FROM cpfDB.cpf_schema_installation
WHERE (LOWER(schema_name), system_code) IN (
    ('cpfdb', 'CPF'), ('cmndb', 'CMN'), ('admdb', 'ADM'), ('bzadb', 'BZA'),
    ('batdb', 'BAT'), ('refdb', 'REF')
)
  AND database_vendor = 'MARIADB'
  AND baseline_key = 'CPF_PROFILE_INSTALL_V1'
  AND install_state = 'PRODUCT_SEEDED';

-- cpf_transaction_meta.transaction_id는 실행 ID가 아니라 업무 거래 정의 ID이므로
-- 34자리 Runtime transactionId 폭 검사에서 명시적으로 제외합니다.
SELECT 'platform.runtime_transaction_id_width' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.columns
WHERE LOWER(table_schema) IN (
    'cpfdb', 'cmndb', 'admdb', 'bzadb',
    'batdb', 'refdb'
)
  AND LOWER(column_name) = 'transaction_id'
  AND NOT (
      LOWER(table_schema) = 'cpfdb'
      AND LOWER(table_name) = 'cpf_transaction_meta'
  )
  AND (
      LOWER(data_type) <> 'char'
      OR character_maximum_length <> 34
  );

SELECT 'cpfDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM cpfDB.cpf_code) >= 100
           AND (SELECT COUNT(*) FROM cpfDB.cpf_message) >= 40
           AND (SELECT COUNT(*) FROM cpfDB.cpf_response_code) >= 40
           AND (SELECT COUNT(*) FROM cpfDB.cpf_config) >= 20,
           1, 0
       ) AS passed;

SELECT 'admDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM admDB.adm_role WHERE USE_YN = 'Y') >= 5
           AND (SELECT COUNT(*) FROM admDB.adm_menu WHERE USE_YN = 'Y') >= 30
           AND (SELECT COUNT(*) FROM admDB.adm_api_permission WHERE USE_YN = 'Y') >= 10,
           1, 0
       ) AS passed;

SELECT 'bzaDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM bzaDB.bza_role WHERE use_yn = 'Y') >= 4
           AND (SELECT COUNT(*) FROM bzaDB.bza_menu WHERE use_yn = 'Y') >= 8
           AND (SELECT COUNT(*) FROM bzaDB.bza_permission
                WHERE role_code = 'BZA_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8,
           1, 0
       ) AS passed;

SELECT 'platform.removed_stale_tables_absent' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE
    (LOWER(table_schema) = 'cpfdb' AND LOWER(table_name) = 'cpf_file_exchange_log')
    OR (LOWER(table_schema) = 'admdb' AND LOWER(table_name) = 'adm_operation_log')
    OR (
        LOWER(table_schema) = 'bzadb'
        AND LOWER(table_name) IN (
            'bza_customer', 'bza_product', 'bza_order', 'bza_masking_audit'
        )
    );

-- V61 admin data safety status verification
SELECT 'VERIFY adm_operator account safety columns' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
  FROM information_schema.columns
 WHERE LOWER(table_schema)='admdb' AND LOWER(table_name)='adm_operator'
   AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');

SELECT 'VERIFY bza_admin_user account safety columns' AS check_name,
       IF(COUNT(*) = 2, 1, 0) AS passed
  FROM information_schema.columns
 WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_admin_user'
   AND LOWER(column_name) IN ('account_status','version_no');

SELECT 'VERIFY BZA employee status default' AS check_name,
       IF(MAX(UPPER(TRIM(BOTH '\'' FROM COALESCE(column_default,'')))) = 'EMPLOYED', 1, 0) AS passed
  FROM information_schema.columns
 WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_employee' AND LOWER(column_name)='employment_status';

SELECT 'VERIFY ADM contact ownership' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.columns WHERE LOWER(table_schema)='admdb' AND LOWER(table_name)='adm_operator' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 0
         AND
         (SELECT COUNT(*) FROM information_schema.columns WHERE LOWER(table_schema)='admdb' AND LOWER(table_name)='adm_operator_profile' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 2,
         1, 0
       ) AS passed;

SELECT 'VERIFY V61 status catalog constraints' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.table_constraints WHERE LOWER(table_schema)='admdb' AND LOWER(table_name)='adm_operator' AND constraint_name='ck_adm_operator_status') = 1
         AND
         (SELECT COUNT(*) FROM information_schema.table_constraints WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_admin_user' AND constraint_name='ck_bza_admin_user_status') = 1
         AND
         (SELECT COUNT(*) FROM information_schema.table_constraints WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_employee' AND constraint_name='ck_bza_employee_status') = 1,
         1, 0
       ) AS passed;

-- V62/V63 BZA idempotency and login-operation verification
SELECT 'VERIFY V62 bootstrap operation id' AS check_name,
       IF(COUNT(*) = 1, 1, 0) AS passed
  FROM information_schema.columns
 WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_admin_user'
   AND LOWER(column_name)='create_operation_id';

SELECT 'VERIFY V63 login operation table' AS check_name,
       IF(COUNT(*) = 1, 1, 0) AS passed
  FROM information_schema.tables
 WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_login_operation'
   AND table_type='BASE TABLE';

SELECT 'VERIFY V63 refresh login operation link' AS check_name,
       IF(COUNT(*) = 1, 1, 0) AS passed
  FROM information_schema.columns
 WHERE LOWER(table_schema)='bzadb' AND LOWER(table_name)='bza_refresh_token'
   AND LOWER(column_name)='login_operation_id';

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       IF(
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE LOWER(table_schema) = 'batdb'
               AND table_type = 'SEQUENCE') = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE LOWER(table_schema) = 'batdb'
               AND table_type = 'SEQUENCE'
               AND UPPER(table_name) IN ('BATCH_JOB_INSTANCE_SEQ', 'BATCH_JOB_EXECUTION_SEQ', 'BATCH_STEP_EXECUTION_SEQ')) = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE LOWER(table_schema) = 'batdb'
               AND UPPER(table_name) IN ('BATCH_JOB_SEQ')) = 0,
           1, 0
       ) AS passed;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
