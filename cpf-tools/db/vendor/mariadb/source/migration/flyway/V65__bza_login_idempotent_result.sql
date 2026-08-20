ALTER TABLE mbw_login_operation
  ADD COLUMN request_hash VARCHAR(64) NOT NULL DEFAULT '0000000000000000000000000000000000000000000000000000000000000000',
  ADD COLUMN result_access_token_enc LONGTEXT NULL,
  ADD COLUMN result_refresh_token_enc LONGTEXT NULL,
  ADD COLUMN result_refresh_expires_at DATETIME(3) NULL,
  ADD COLUMN result_expires_at DATETIME(3) NULL,
  ADD COLUMN failure_code VARCHAR(80) NULL,
  ADD COLUMN failure_message VARCHAR(500) NULL;
ALTER TABLE mbw_login_operation DROP CONSTRAINT ck_mbw_login_operation_status;
ALTER TABLE mbw_login_operation ADD CONSTRAINT ck_mbw_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED'));
CREATE INDEX ix_mbw_login_operation_expiry ON mbw_login_operation(operation_status,result_expires_at);
ALTER TABLE mbw_login_operation ALTER COLUMN request_hash DROP DEFAULT;
