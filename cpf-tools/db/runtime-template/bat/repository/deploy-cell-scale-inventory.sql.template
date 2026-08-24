SELECT d.instance_id, COALESCE(r.actual_state, 'STOPPED') actual_state,
       COALESCE(h.current_execution_count, 0) current_execution_count
FROM BAT_DEPLOYMENT_INSTANCE d
LEFT JOIN BAT_RUNTIME_INSTANCE r ON r.instance_id = d.instance_id
LEFT JOIN (
    SELECT instance_id, current_execution_count
    FROM (
        SELECT instance_id, current_execution_count,
               ROW_NUMBER() OVER (PARTITION BY instance_id ORDER BY heartbeat_at DESC) rn
        FROM BAT_RUNTIME_HEARTBEAT
    ) ranked
    WHERE rn = 1
) h ON h.instance_id = d.instance_id
WHERE d.cell_id = ?
ORDER BY d.instance_id
