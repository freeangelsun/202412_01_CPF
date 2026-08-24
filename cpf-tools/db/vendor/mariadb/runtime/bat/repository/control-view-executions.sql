SELECT execution_id, job_id, execution_status, worker_id, business_date, start_time, end_time,
       last_heartbeat_at, retry_count
FROM BAT_EXECUTION
ORDER BY execution_id DESC
LIMIT 500
