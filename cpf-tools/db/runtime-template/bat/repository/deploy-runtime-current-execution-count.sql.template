SELECT COALESCE(MAX(CASE WHEN ranked.rn = 1 THEN ranked.current_execution_count END), 0)
FROM (
    SELECT current_execution_count,
           ROW_NUMBER() OVER (ORDER BY heartbeat_at DESC) rn
    FROM BAT_RUNTIME_HEARTBEAT
    WHERE instance_id = ?
) ranked
