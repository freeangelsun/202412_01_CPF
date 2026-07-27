-- CPF oracle install verification generated from canonical model


SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 38 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('cpfDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('cmnDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 29 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('admDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 43 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('batDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 28 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('bzaDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 8 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('mbrDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('accDB');

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 3 THEN 1 ELSE 0 END AS passed
FROM all_tables
WHERE owner = UPPER('refDB');

SELECT 'platform.fixed_exs_schema_absent' AS check_name,
       CASE WHEN COUNT(*) = 0 THEN 1 ELSE 0 END AS passed
FROM all_users WHERE username = 'EXSDB';

SELECT 'v63_login_operation' AS check_name,
       CASE WHEN COUNT(*) = 1 THEN 1 ELSE 0 END AS passed
FROM all_tables WHERE owner='BZADB' AND table_name='BZA_LOGIN_OPERATION';

SELECT 'v63_refresh_link' AS check_name,
       CASE WHEN COUNT(*) = 1 THEN 1 ELSE 0 END AS passed
FROM all_tab_columns
WHERE owner='BZADB' AND table_name='BZA_REFRESH_TOKEN' AND column_name='LOGIN_OPERATION_ID';

SELECT 'product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM cpf_code) >= 100
           AND (SELECT COUNT(*) FROM cpf_message) >= 40
           AND (SELECT COUNT(*) FROM cpf_response_code) >= 40
           AND (SELECT COUNT(*) FROM cpf_config) >= 20
       THEN 1 ELSE 0 END AS passed FROM dual;

SELECT 'product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM bza_role WHERE use_yn='Y') >= 4
           AND (SELECT COUNT(*) FROM bza_menu WHERE use_yn='Y') >= 8
           AND (SELECT COUNT(*) FROM bza_permission WHERE role_code='BZA_ADMIN' AND allow_yn='Y' AND use_yn='Y') >= 8
       THEN 1 ELSE 0 END AS passed FROM dual;
