-- V57 이전 개발 Schema 폭으로 되돌립니다. 데이터는 삭제하지 않습니다.
USE admDB;

ALTER TABLE adm_audit_log
    MODIFY COLUMN TRANSACTION_ID VARCHAR(80) NULL COMMENT '프레임워크 거래 ID';

ALTER TABLE adm_audit_delivery
    MODIFY COLUMN TRANSACTION_ID VARCHAR(64) NOT NULL COMMENT 'CPF 전역 transactionId';
