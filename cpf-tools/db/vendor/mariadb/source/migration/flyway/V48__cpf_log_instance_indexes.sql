-- ADM에서 Domain/Instance/WAS 교차 검색을 안정적으로 지원하는 운영 인덱스
CREATE INDEX IF NOT EXISTS ix_cpf_transaction_log_was_time
    ON cpf_transaction_log(WAS_ID, START_TIME);
CREATE INDEX IF NOT EXISTS ix_cpf_transaction_log_module_instance_time
    ON cpf_transaction_log(MODULE_ID, SERVER_INSTANCE_ID, START_TIME);
