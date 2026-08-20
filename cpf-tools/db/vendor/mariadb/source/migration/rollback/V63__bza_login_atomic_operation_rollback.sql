USE backofficeDB;

ALTER TABLE mbw_refresh_token
    DROP INDEX ix_mbw_refresh_token_login_operation,
    DROP COLUMN login_operation_id;

DROP TABLE IF EXISTS mbw_login_operation;
