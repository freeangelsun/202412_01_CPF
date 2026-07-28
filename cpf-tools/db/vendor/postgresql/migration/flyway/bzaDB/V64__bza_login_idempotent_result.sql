ALTER TABLE bza_login_operation ADD COLUMN request_hash VARCHAR(64) NOT NULL DEFAULT '0000000000000000000000000000000000000000000000000000000000000000';
ALTER TABLE bza_login_operation ADD COLUMN result_access_token_enc TEXT; ALTER TABLE bza_login_operation ADD COLUMN result_refresh_token_enc TEXT;
ALTER TABLE bza_login_operation ADD COLUMN result_refresh_expires_at TIMESTAMP(3); ALTER TABLE bza_login_operation ADD COLUMN result_expires_at TIMESTAMP(3);
ALTER TABLE bza_login_operation ADD COLUMN failure_code VARCHAR(80); ALTER TABLE bza_login_operation ADD COLUMN failure_message VARCHAR(500);
ALTER TABLE bza_login_operation DROP CONSTRAINT IF EXISTS ck_bza_login_operation_status;
ALTER TABLE bza_login_operation ADD CONSTRAINT ck_bza_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED'));
CREATE INDEX ix_bza_login_operation_expiry ON bza_login_operation(operation_status,result_expires_at);
ALTER TABLE bza_login_operation ALTER COLUMN request_hash DROP DEFAULT;
