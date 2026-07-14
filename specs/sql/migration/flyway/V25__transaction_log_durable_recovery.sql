-- PFW DB 거래 로그 durable fallback 재적재의 중복 방지 키를 추가합니다.
USE pfwDB;

ALTER TABLE pfw_transaction_log
    ADD COLUMN IF NOT EXISTS RECOVERY_EVENT_ID VARCHAR(64) NULL
        COMMENT 'DB 로그 복구 이벤트 중복 방지 ID'
        AFTER LOG_IDX;

CREATE UNIQUE INDEX IF NOT EXISTS uk_pfw_transaction_log_recovery_event
    ON pfw_transaction_log (RECOVERY_EVENT_ID);
