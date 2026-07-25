-- Batch 실행을 server instance 기준으로 빠르게 추적하기 위한 운영 인덱스
CREATE INDEX IF NOT EXISTS ix_bat_execution_server
    ON bat_execution(server_instance_id, start_time);
