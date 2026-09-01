-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json
-- vendor=mariadb; platform service users and least-privilege grants
-- The caller must set @cpf_migration_password and @cpf_app_password in-memory.
-- DO NOT EDIT generated provision SQL directly.

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_mbw_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_mbw_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cpfDB.* TO 'cpf_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON cpfDB.* TO 'cpf_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON mbwDB.* TO 'cpf_mbw_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON mbwDB.* TO 'cpf_mbw_app'@'%';

SET @cpf_sql = NULL;
FLUSH PRIVILEGES;
