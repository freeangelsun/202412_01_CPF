ALTER TABLE bza_login_operation ADD (request_hash VARCHAR2(64) DEFAULT '0000000000000000000000000000000000000000000000000000000000000000' NOT NULL,result_access_token_enc CLOB,result_refresh_token_enc CLOB,result_refresh_expires_at TIMESTAMP(3),result_expires_at TIMESTAMP(3),failure_code VARCHAR2(80),failure_message VARCHAR2(500));
ALTER TABLE bza_login_operation DROP CONSTRAINT ck_bza_login_operation_status;
ALTER TABLE bza_login_operation ADD CONSTRAINT ck_bza_login_operation_status CHECK(operation_status IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED'));
CREATE INDEX ix_bza_login_operation_expiry ON bza_login_operation(operation_status,result_expires_at);
ALTER TABLE bza_login_operation MODIFY (request_hash DEFAULT NULL);
