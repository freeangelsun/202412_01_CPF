-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=oracle; each logical section executes in its profile-selected schema.
-- DO NOT EDIT generated verify SQL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
SELECT 'cpfDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 69 THEN 1 ELSE 0 END AS passed
FROM user_tables;

SELECT 'cpfDB.product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM cpf_code) >= 100
           AND (SELECT COUNT(*) FROM cpf_message) >= 40
           AND (SELECT COUNT(*) FROM cpf_response_code) >= 40
           AND (SELECT COUNT(*) FROM cpf_config) >= 20
       THEN 1 ELSE 0 END AS passed FROM dual;

-- CPF_LOGICAL_DATABASE=cmnDB
SELECT 'cmnDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END AS passed
FROM user_tables;

-- CPF_LOGICAL_DATABASE=admDB
SELECT 'admDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 33 THEN 1 ELSE 0 END AS passed
FROM user_tables;

-- CPF_LOGICAL_DATABASE=bzaDB
SELECT 'bzaDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 29 THEN 1 ELSE 0 END AS passed
FROM user_tables;

SELECT 'bzaDB.product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM bza_role WHERE use_yn = 'Y') >= 4
           AND (SELECT COUNT(*) FROM bza_menu WHERE use_yn = 'Y') >= 8
           AND (SELECT COUNT(*) FROM bza_permission WHERE role_code = 'BZA_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8
       THEN 1 ELSE 0 END AS passed FROM dual;

-- CPF_LOGICAL_DATABASE=batDB
SELECT 'batDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 56 THEN 1 ELSE 0 END AS passed
FROM user_tables;

-- CPF_LOGICAL_DATABASE=refDB
SELECT 'refDB.table_count' AS check_name,
       CASE WHEN COUNT(*) = 3 THEN 1 ELSE 0 END AS passed
FROM user_tables;

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- CPF_LOGICAL_DATABASE=batDB
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM user_sequences) = 3
           AND
           (SELECT COUNT(*) FROM user_sequences
             WHERE sequence_name IN ('BATCH_JOB_INSTANCE_SEQ', 'BATCH_JOB_EXECUTION_SEQ', 'BATCH_STEP_EXECUTION_SEQ')) = 3
           AND
           (SELECT COUNT(*) FROM user_objects
             WHERE object_name IN ('BATCH_JOB_SEQ')) = 0
       THEN 1 ELSE 0 END AS passed
FROM dual;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
