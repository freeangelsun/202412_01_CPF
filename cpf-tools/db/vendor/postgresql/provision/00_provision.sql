-- CPF PostgreSQL provisioning template. Execute as a privileged deployment account.
-- Physical database/user names are supplied by the install profile; the CPF runner performs CREATE DATABASE/ROLE separately.
SELECT current_database() AS connected_database;
