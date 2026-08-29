-- CPF generated SQL bundle: 00_provision.sql
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 01_create_databases.sql =====
-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json
-- vendor=mariadb; platform provision databases
-- DO NOT EDIT generated provision SQL directly.

CREATE DATABASE IF NOT EXISTS cpfDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mbwDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
-- ===== END 01_create_databases.sql =====

-- ===== BEGIN 02_create_service_users.sql =====
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
-- ===== END 02_create_service_users.sql =====
