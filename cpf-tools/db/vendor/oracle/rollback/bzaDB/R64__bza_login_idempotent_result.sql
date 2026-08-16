DROP INDEX ix_bza_login_operation_expiry; ALTER TABLE bza_login_operation DROP CONSTRAINT ck_bza_login_operation_status;
ALTER TABLE bza_login_operation ADD CONSTRAINT ck_bza_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS'));
ALTER TABLE bza_login_operation DROP (failure_message,failure_code,result_expires_at,result_refresh_expires_at,result_refresh_token_enc,result_access_token_enc,request_hash);
