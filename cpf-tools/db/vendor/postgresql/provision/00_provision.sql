-- CPF generated lifecycle bundle; vendor=postgresql
-- Source plan: cpf-tools/config/database-source-plan.json

-- ===== BEGIN 00_provision.sql =====
-- AUTO-GENERATED from cpf-tools/config/database-install.default.json
-- vendor=postgresql; physical database/roles are created by the profile-aware executor.
-- DO NOT EDIT generated provision SQL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
CREATE SCHEMA IF NOT EXISTS cpfDB;

-- CPF_LOGICAL_DATABASE=cmnDB
CREATE SCHEMA IF NOT EXISTS cmnDB;

-- CPF_LOGICAL_DATABASE=admDB
CREATE SCHEMA IF NOT EXISTS admDB;

-- CPF_LOGICAL_DATABASE=bzaDB
CREATE SCHEMA IF NOT EXISTS bzaDB;

-- CPF_LOGICAL_DATABASE=batDB
CREATE SCHEMA IF NOT EXISTS batDB;

-- CPF_LOGICAL_DATABASE=refDB
CREATE SCHEMA IF NOT EXISTS refDB;

-- ===== END 00_provision.sql =====
