SELECT d.instance_id, d.desired_state, COALESCE(r.actual_state, 'STOPPED') actual_state,
       r.last_heartbeat_at
FROM BAT_DEPLOYMENT_INSTANCE d
LEFT JOIN BAT_RUNTIME_INSTANCE r ON r.instance_id = d.instance_id
WHERE d.cell_id = ?
ORDER BY d.instance_id
