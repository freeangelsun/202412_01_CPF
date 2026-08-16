-- CPF DB 거래 로그 durable fallback 재적재의 중복 방지 키를 추가합니다.
USE cpfDB;

ALTER TABLE cpf_transaction_log
    ADD COLUMN IF NOT EXISTS RECOVERY_EVENT_ID VARCHAR(64) NULL
        COMMENT 'DB 로그 복구 이벤트 중복 방지 ID'
        AFTER LOG_IDX;

CREATE UNIQUE INDEX IF NOT EXISTS uk_cpf_transaction_log_recovery_event
    ON cpf_transaction_log (RECOVERY_EVENT_ID);
