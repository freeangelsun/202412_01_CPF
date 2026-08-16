SELECT r.instance_id, r.host_alias, r.zone_id, r.artifact_version, r.actual_state,
       r.last_heartbeat_at, r.capabilities
FROM (
    SELECT i.*,
           (SELECT STRING_AGG(c.capability_code, ',' ORDER BY c.capability_code)
              FROM bat_runtime_capability c
             WHERE c.instance_id = i.instance_id) capabilities
    FROM bat_runtime_instance i
) r
WHERE r.runtime_role = 'AGENT'
ORDER BY r.host_alias, r.instance_id
