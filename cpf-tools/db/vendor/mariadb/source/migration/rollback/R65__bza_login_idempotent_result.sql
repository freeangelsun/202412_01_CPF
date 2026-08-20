DROP INDEX ix_mbw_login_operation_expiry ON mbw_login_operation;
ALTER TABLE mbw_login_operation DROP CONSTRAINT ck_mbw_login_operation_status;
ALTER TABLE mbw_login_operation ADD CONSTRAINT ck_mbw_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS'));
ALTER TABLE mbw_login_operation
  DROP COLUMN failure_message, DROP COLUMN failure_code, DROP COLUMN result_expires_at, DROP COLUMN result_refresh_expires_at,
  DROP COLUMN result_refresh_token_enc, DROP COLUMN result_access_token_enc, DROP COLUMN request_hash;
