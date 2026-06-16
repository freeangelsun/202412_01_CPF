-- CPF initial database smoke check.
-- Run after 01_create_databases.sql through 70_test_data.sql.

SELECT 'pfwDB.TRAN_LOG' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG;
SELECT 'pfwDB.TRAN_LOG_DTL' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG_DTL;

SELECT 'cmnDB.code_table' AS check_name, COUNT(*) AS row_count FROM cmnDB.code_table;
SELECT 'cmnDB.message_table' AS check_name, COUNT(*) AS row_count FROM cmnDB.message_table;
SELECT 'cmnDB.response_code_table' AS check_name, COUNT(*) AS row_count FROM cmnDB.response_code_table;
SELECT 'cmnDB.config_table' AS check_name, COUNT(*) AS row_count FROM cmnDB.config_table;
SELECT 'cmnDB.cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM cmnDB.cache_refresh_event;

SELECT 'admDB.operator_user' AS check_name, COUNT(*) AS row_count FROM admDB.operator_user;
SELECT 'admDB.operator_menu' AS check_name, COUNT(*) AS row_count FROM admDB.operator_menu;
SELECT 'admDB.operator_role' AS check_name, COUNT(*) AS row_count FROM admDB.operator_role;

SELECT 'accDB.acc_member' AS check_name, COUNT(*) AS row_count FROM accDB.acc_member;
SELECT 'cmnDB.member' AS check_name, COUNT(*) AS row_count FROM cmnDB.member;
SELECT 'mbrDB.member' AS check_name, COUNT(*) AS row_count FROM mbrDB.member;

SELECT response_code, message_code, result_type, http_status
FROM cmnDB.response_code_table
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM cmnDB.message_table
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;
