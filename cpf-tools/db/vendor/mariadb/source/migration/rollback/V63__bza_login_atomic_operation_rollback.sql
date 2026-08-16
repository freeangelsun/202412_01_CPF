USE bzaDB;

ALTER TABLE bza_refresh_token
    DROP INDEX ix_bza_refresh_token_login_operation,
    DROP COLUMN login_operation_id;

DROP TABLE IF EXISTS bza_login_operation;
