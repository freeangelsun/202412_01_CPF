-- CPF postgresql install verification generated from canonical model


SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 38 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('cpfDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('cmnDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 29 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('admDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 43 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('batDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 28 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('bzaDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 8 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('mbrDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('accDB') AND table_type = 'BASE TABLE';

SELECT 'table_count' AS check_name,
       CASE WHEN COUNT(*) = 3 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema) = lower('refDB') AND table_type = 'BASE TABLE';

SELECT 'platform.fixed_exs_schema_absent' AS check_name,
       CASE WHEN COUNT(*) = 0 THEN 1 ELSE 0 END AS passed
FROM information_schema.schemata WHERE lower(schema_name)='exsdb';

SELECT 'v63_login_operation' AS check_name,
       CASE WHEN COUNT(*) = 1 THEN 1 ELSE 0 END AS passed
FROM information_schema.tables
WHERE lower(table_schema)='bzadb' AND lower(table_name)='bza_login_operation';

SELECT 'v63_refresh_link' AS check_name,
       CASE WHEN COUNT(*) = 1 THEN 1 ELSE 0 END AS passed
FROM information_schema.columns
WHERE lower(table_schema)='bzadb' AND lower(table_name)='bza_refresh_token'
  AND lower(column_name)='login_operation_id';

SELECT 'product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM cpf_code) >= 100
           AND (SELECT COUNT(*) FROM cpf_message) >= 40
           AND (SELECT COUNT(*) FROM cpf_response_code) >= 40
           AND (SELECT COUNT(*) FROM cpf_config) >= 20
       THEN 1 ELSE 0 END AS passed;

SELECT 'product_seed' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM bza_role WHERE use_yn='Y') >= 4
           AND (SELECT COUNT(*) FROM bza_menu WHERE use_yn='Y') >= 8
           AND (SELECT COUNT(*) FROM bza_permission WHERE role_code='BZA_ADMIN' AND allow_yn='Y' AND use_yn='Y') >= 8
       THEN 1 ELSE 0 END AS passed;
