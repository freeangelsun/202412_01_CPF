-- CPF 로컬/테스트용 최소 권한 계정 생성 스크립트입니다.
-- 운영 환경에서는 같은 계정 구조를 유지하되 비밀번호는 Vault/KMS 또는 배포 환경변수로 주입합니다.

CREATE USER IF NOT EXISTS 'cpf_pfw_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_cmn_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_xyz_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_bizadm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_exs_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';

CREATE USER IF NOT EXISTS 'cpf_pfw_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_cmn_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_xyz_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_bizadm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_exs_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_cmn_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_xyz_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_bizadm_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_exs_migration'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_cmn_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_xyz_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_bizadm_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_exs_app'@'localhost' IDENTIFIED BY 'cpf_local_pw';

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
