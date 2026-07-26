SELECT d.instance_id, d.host_alias, d.zone_id, d.pool_id, d.desired_state,
       r.actual_state, r.last_heartbeat_at, r.artifact_version, r.row_version,
       COALESCE(h.current_execution_count, 0) current_execution_count
FROM bat_deployment_instance d
LEFT JOIN bat_runtime_instance r ON r.instance_id = d.instance_id
LEFT JOIN (
    SELECT instance_id, current_execution_count
    FROM (
        SELECT instance_id, current_execution_count,
               ROW_NUMBER() OVER (PARTITION BY instance_id ORDER BY heartbeat_at DESC) rn
        FROM bat_runtime_heartbeat
    ) ranked
    WHERE rn = 1
) h ON h.instance_id = d.instance_id
WHERE d.cell_id = ?
ORDER BY d.instance_id
