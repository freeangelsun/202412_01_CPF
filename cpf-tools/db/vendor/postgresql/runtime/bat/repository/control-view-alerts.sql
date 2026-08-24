SELECT instance_id, runtime_role, actual_state, last_heartbeat_at
FROM BAT_RUNTIME_INSTANCE
WHERE actual_state IN ('FAILED', 'DEGRADED', 'UNKNOWN')
   OR last_heartbeat_at < CURRENT_TIMESTAMP(6) - INTERVAL '30 seconds'
ORDER BY runtime_role, instance_id
