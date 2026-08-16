-- CPF runtime transactionId를 34자리 단일 정본으로 통일합니다.
ALTER TABLE cpf_security_token_audit_log MODIFY COLUMN TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF transactionId';
ALTER TABLE cpf_saga_execution MODIFY COLUMN transaction_id CHAR(34) NULL COMMENT 'CPF transactionId';
