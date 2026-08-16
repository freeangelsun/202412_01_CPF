DROP INDEX IF EXISTS ix_bza_login_operation_expiry; ALTER TABLE bza_login_operation DROP CONSTRAINT IF EXISTS ck_bza_login_operation_status;
ALTER TABLE bza_login_operation ADD CONSTRAINT ck_bza_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS'));
ALTER TABLE bza_login_operation DROP COLUMN IF EXISTS failure_message, DROP COLUMN IF EXISTS failure_code, DROP COLUMN IF EXISTS result_expires_at, DROP COLUMN IF EXISTS result_refresh_expires_at, DROP COLUMN IF EXISTS result_refresh_token_enc, DROP COLUMN IF EXISTS result_access_token_enc, DROP COLUMN IF EXISTS request_hash;
