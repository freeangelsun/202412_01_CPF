SELECT r.instance_id, r.host_alias, r.zone_id, r.artifact_version, r.actual_state,
       r.last_heartbeat_at, r.capabilities
FROM (
    SELECT i.*,
           (SELECT LISTAGG(c.capability_code, ',') WITHIN GROUP (ORDER BY c.capability_code)
              FROM BAT_RUNTIME_CAPABILITY c
             WHERE c.instance_id = i.instance_id) capabilities
    FROM BAT_RUNTIME_INSTANCE i
) r
WHERE r.runtime_role = 'AGENT'
ORDER BY r.host_alias, r.instance_id
