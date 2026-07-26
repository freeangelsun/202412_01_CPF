SELECT execution_id, job_id, execution_status, worker_id, business_date, start_time, end_time,
       last_heartbeat_at, retry_count
FROM bat_execution
ORDER BY execution_id DESC
FETCH FIRST 500 ROWS ONLY
