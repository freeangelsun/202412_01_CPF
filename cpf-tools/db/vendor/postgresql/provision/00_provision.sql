-- CPF generated lifecycle bundle; vendor=postgresql
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 00_provision.sql =====
-- AUTO-GENERATED from cpf-tools/db/config/database-install.default.json
-- vendor=postgresql; physical database/roles are created by the profile-aware executor.
-- DO NOT EDIT generated provision SQL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
CREATE SCHEMA IF NOT EXISTS cpfDB;

-- CPF_LOGICAL_DATABASE=bzaDB
CREATE SCHEMA IF NOT EXISTS bzaDB;

-- CPF_LOGICAL_DATABASE=referenceFixture
CREATE SCHEMA IF NOT EXISTS referenceFixture;

-- ===== END 00_provision.sql =====
