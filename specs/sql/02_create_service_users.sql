-- Local least-privilege service users for CPF modules.
-- These accounts are for local/test only. Change passwords or use environment variables in real deployments.

CREATE USER IF NOT EXISTS 'cpf_pfw'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_adm'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_acc'@'localhost' IDENTIFIED BY 'cpf_local_pw';
CREATE USER IF NOT EXISTS 'cpf_mbr'@'localhost' IDENTIFIED BY 'cpf_local_pw';

ALTER USER 'cpf_pfw'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_adm'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_acc'@'localhost' IDENTIFIED BY 'cpf_local_pw';
ALTER USER 'cpf_mbr'@'localhost' IDENTIFIED BY 'cpf_local_pw';

GRANT SELECT, INSERT, UPDATE, DELETE ON pfwDB.* TO 'cpf_pfw'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON admDB.* TO 'cpf_adm'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON accDB.* TO 'cpf_acc'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON mbrDB.* TO 'cpf_mbr'@'localhost';

FLUSH PRIVILEGES;
