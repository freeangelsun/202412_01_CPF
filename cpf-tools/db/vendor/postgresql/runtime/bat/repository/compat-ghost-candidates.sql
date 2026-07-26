SELECT *
FROM bat_execution
WHERE execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING')
  AND last_heartbeat_at < CURRENT_TIMESTAMP(3) - (? * INTERVAL '1 second')
ORDER BY last_heartbeat_at
