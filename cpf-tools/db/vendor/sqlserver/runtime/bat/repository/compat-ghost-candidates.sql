SELECT *
FROM bat_execution
WHERE execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING')
  AND last_heartbeat_at < DATEADD(SECOND, (0 - ?), SYSUTCDATETIME())
ORDER BY last_heartbeat_at
