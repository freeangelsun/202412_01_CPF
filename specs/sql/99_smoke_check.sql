-- CPF initial database smoke check.
-- Run after 01_create_databases.sql through 70_test_data.sql.

SELECT 'pfwDB.TRAN_LOG' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG;
SELECT 'pfwDB.TRAN_LOG_DTL' AS check_name, COUNT(*) AS row_count FROM pfwDB.TRAN_LOG_DTL;

SELECT 'pfwDB.code_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.code_table;
SELECT 'pfwDB.message_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.message_table;
SELECT 'pfwDB.response_code_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.response_code_table;
SELECT 'pfwDB.config_table' AS check_name, COUNT(*) AS row_count FROM pfwDB.config_table;
SELECT 'pfwDB.cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.cache_refresh_event;

SELECT 'admDB.operator_user' AS check_name, COUNT(*) AS row_count FROM admDB.operator_user;
SELECT 'admDB.operator_menu' AS check_name, COUNT(*) AS row_count FROM admDB.operator_menu;
SELECT 'admDB.operator_role' AS check_name, COUNT(*) AS row_count FROM admDB.operator_role;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'mbrDB.member' AS check_name, COUNT(*) AS row_count FROM mbrDB.member;

SELECT response_code, message_code, result_type, http_status
FROM pfwDB.response_code_table
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM pfwDB.message_table
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;
