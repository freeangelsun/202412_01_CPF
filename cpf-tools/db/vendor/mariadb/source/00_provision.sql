-- CPF generated SQL bundle: 00_provision.sql
-- 목적: 관리자 권한으로 Schema와 migration/runtime 최소 권한 계정을 명시적으로 Provision
-- 정본은 database-source-plan.json의 mariadb.sourceRoot 아래 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File cpf-tools/db/tools/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/01_create_databases.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json
-- vendor=mariadb; platform provision databases
-- DO NOT EDIT generated provision SQL directly.

CREATE DATABASE IF NOT EXISTS cpfDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS cpfDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mbwDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/02_create_service_users.sql
-- ============================================================================
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

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cpfDB.* TO 'cpf_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON cpfDB.* TO 'cpf_app'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON mbwDB.* TO 'cpf_mbw_migration'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON mbwDB.* TO 'cpf_mbw_app'@'%';

SET @cpf_sql = NULL;
FLUSH PRIVILEGES;
