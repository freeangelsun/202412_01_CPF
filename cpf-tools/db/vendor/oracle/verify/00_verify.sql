-- CPF Oracle structural verification
SELECT SYS_CONTEXT('USERENV','DB_NAME') AS database_name, SYS_CONTEXT('USERENV','CURRENT_SCHEMA') AS current_schema FROM dual;
SELECT COUNT(*) AS cpf_table_count FROM user_tables;
