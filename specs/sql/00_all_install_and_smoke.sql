-- CPF all-in-one install and smoke-check script.
-- Run this file from the repository root:
-- mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install_and_smoke.sql

SOURCE specs/sql/00_all_install.sql;
SOURCE specs/sql/99_smoke_check.sql;
