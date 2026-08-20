-- V62 exact rollback
USE backofficeDB;
ALTER TABLE mbw_admin_user DROP INDEX IF EXISTS uk_mbw_admin_user_create_operation;
ALTER TABLE mbw_admin_user DROP COLUMN IF EXISTS create_operation_id;
