-- V62 exact rollback
USE bzaDB;
ALTER TABLE bza_admin_user DROP INDEX IF EXISTS uk_bza_admin_user_create_operation;
ALTER TABLE bza_admin_user DROP COLUMN IF EXISTS create_operation_id;
