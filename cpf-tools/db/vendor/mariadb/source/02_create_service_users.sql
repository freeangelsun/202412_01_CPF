-- AUTO-GENERATED from cpf-tools/config/database-install.default.json
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

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_cmn_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_cmn_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_adm_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_adm_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_bza_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_bza_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_bat_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_bat_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_ref_migration''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS ''cpf_ref_app''@''%'' IDENTIFIED BY ', QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql;
EXECUTE cpf_user_stmt;
DEALLOCATE PREPARE cpf_user_stmt;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cpfDB.* TO 'cpf_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON cpfDB.* TO 'cpf_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cmnDB.* TO 'cpf_cmn_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON cmnDB.* TO 'cpf_cmn_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON admDB.* TO 'cpf_adm_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON admDB.* TO 'cpf_adm_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON bzaDB.* TO 'cpf_bza_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON bzaDB.* TO 'cpf_bza_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON batDB.* TO 'cpf_bat_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON batDB.* TO 'cpf_bat_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON refDB.* TO 'cpf_ref_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON refDB.* TO 'cpf_ref_app'@'%';

SET @cpf_sql = NULL;
FLUSH PRIVILEGES;
