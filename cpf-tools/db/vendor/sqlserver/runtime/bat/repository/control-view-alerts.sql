SELECT instance_id, runtime_role, actual_state, last_heartbeat_at
FROM bat_runtime_instance
WHERE actual_state IN ('FAILED', 'DEGRADED', 'UNKNOWN')
   OR last_heartbeat_at < DATEADD(SECOND, -30, SYSUTCDATETIME())
ORDER BY runtime_role, instance_id
