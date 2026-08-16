-- R10 이전 개발 스키마 폭으로 되돌립니다.
ALTER TABLE cpf_security_token_audit_log MODIFY COLUMN TRANSACTION_ID VARCHAR(80) NULL COMMENT '전역 거래 ID';
ALTER TABLE cpf_saga_execution MODIFY COLUMN transaction_id VARCHAR(100) NULL;
