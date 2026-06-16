-- CPF all-in-one install script.
-- Run this file from the repository root:
-- mysql -h localhost -P 3306 -u root -p < specs/sql/00_all_install.sql

SOURCE specs/sql/01_create_databases.sql;
SOURCE specs/sql/10_pfw_schema.sql;
SOURCE specs/sql/20_cmn_schema.sql;
SOURCE specs/sql/30_adm_schema.sql;
SOURCE specs/sql/40_business_sample_schema.sql;
SOURCE specs/sql/50_framework_seed_data.sql;
SOURCE specs/sql/60_adm_seed_data.sql;
SOURCE specs/sql/70_test_data.sql;
