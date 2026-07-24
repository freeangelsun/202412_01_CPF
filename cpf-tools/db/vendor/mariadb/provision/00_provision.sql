-- CPF generated SQL bundle: 00_provision.sql
-- 목적: 관리자 권한으로 Schema와 migration/runtime 최소 권한 계정을 명시적으로 Provision
-- 정본은 specs/sql의 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File scripts/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- specs/sql/01_create_databases.sql
-- ============================================================================
-- CPF 초기 데이터베이스 생성 스크립트입니다.
-- 데이터베이스 생성 권한이 있는 migration 계정 또는 root 계정으로 실행합니다.

CREATE DATABASE IF NOT EXISTS cpfDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS cmnDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS admDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS batDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS refDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS exsDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS mbrDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bzaDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- ACC는 생성기 회귀 검증과 중립 업무 reference를 위한 선택 데이터베이스입니다.
CREATE DATABASE IF NOT EXISTS accDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
-- ============================================================================
-- specs/sql/02_create_service_users.sql
-- ============================================================================
-- CPF migration/app 최소 권한 계정 생성 스크립트입니다.
-- 실행 전 동일 MariaDB 세션에 @cpf_migration_password와 @cpf_app_password를 주입해야 합니다.
-- 두 값이 없거나 빈 문자열이면 PREPARE 단계에서 실패하므로 저장소의 고정 비밀번호로 대체되지 않습니다.

SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_cmn_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_adm_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_bat_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_ref_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_exs_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_mbr_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_bza_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_acc_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_cmn_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_adm_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_bat_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_ref_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_exs_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_mbr_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_bza_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE USER IF NOT EXISTS 'cpf_acc_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = NULL;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cpfDB.* TO 'cpf_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cmnDB.* TO 'cpf_cmn_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON admDB.* TO 'cpf_adm_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON batDB.* TO 'cpf_bat_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON refDB.* TO 'cpf_ref_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON exsDB.* TO 'cpf_exs_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON mbrDB.* TO 'cpf_mbr_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON bzaDB.* TO 'cpf_bza_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON accDB.* TO 'cpf_acc_migration'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON cpfDB.* TO 'cpf_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON cmnDB.* TO 'cpf_cmn_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON admDB.* TO 'cpf_adm_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON batDB.* TO 'cpf_bat_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON refDB.* TO 'cpf_ref_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON exsDB.* TO 'cpf_exs_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON mbrDB.* TO 'cpf_mbr_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON bzaDB.* TO 'cpf_bza_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON accDB.* TO 'cpf_acc_app'@'localhost';

FLUSH PRIVILEGES;
