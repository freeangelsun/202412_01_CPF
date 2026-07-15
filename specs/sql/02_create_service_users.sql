-- CPF migration/app 최소 권한 계정 생성 스크립트입니다.
-- 실행 전 동일 MariaDB 세션에 @cpf_migration_password와 @cpf_app_password를 주입해야 합니다.
-- 두 값이 없거나 빈 문자열이면 PREPARE 단계에서 실패하므로 저장소의 고정 비밀번호로 대체되지 않습니다.

SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_pfw_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_cmn_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_adm_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_acc_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_xyz_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_mbr_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_bizadm_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_exs_migration'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_migration_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;

SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_pfw_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_cmn_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_adm_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_acc_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_xyz_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_mbr_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_bizadm_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = CONCAT("CREATE OR REPLACE USER 'cpf_exs_app'@'localhost' IDENTIFIED BY ", QUOTE(NULLIF(@cpf_app_password, '')));
PREPARE cpf_user_stmt FROM @cpf_sql; EXECUTE cpf_user_stmt; DEALLOCATE PREPARE cpf_user_stmt;
SET @cpf_sql = NULL;

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON pfwDB.* TO 'cpf_pfw_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON cmnDB.* TO 'cpf_cmn_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON admDB.* TO 'cpf_adm_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON accDB.* TO 'cpf_acc_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON xyzDB.* TO 'cpf_xyz_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON mbrDB.* TO 'cpf_mbr_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON bizadmDB.* TO 'cpf_bizadm_migration'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON exsDB.* TO 'cpf_exs_migration'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON pfwDB.* TO 'cpf_pfw_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON cmnDB.* TO 'cpf_cmn_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON admDB.* TO 'cpf_adm_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON accDB.* TO 'cpf_acc_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON xyzDB.* TO 'cpf_xyz_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON mbrDB.* TO 'cpf_mbr_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON bizadmDB.* TO 'cpf_bizadm_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON exsDB.* TO 'cpf_exs_app'@'localhost';

FLUSH PRIVILEGES;
