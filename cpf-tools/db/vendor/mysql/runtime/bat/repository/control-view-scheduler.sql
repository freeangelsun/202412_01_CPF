SELECT r.instance_id, r.actual_state, r.artifact_version, r.last_heartbeat_at,
       l.owner_instance_id leader_instance, l.fencing_token, l.lease_until
FROM bat_runtime_instance r
LEFT JOIN bat_scheduler_lease l ON l.scheduler_key = 'BAT_SCHEDULER'
WHERE r.runtime_role = 'SCHEDULER'
ORDER BY r.instance_id
