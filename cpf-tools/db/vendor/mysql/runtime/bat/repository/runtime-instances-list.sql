SELECT i.instance_id, i.runtime_role, i.service_id, i.was_id, i.host_alias, i.zone_id, i.pool_id,
       i.artifact_version, i.git_sha, i.profile_name, i.desired_state,
       CASE
           WHEN i.actual_state = 'FAILED' THEN 'FAILED'
           WHEN i.last_heartbeat_at IS NULL THEN 'UNKNOWN'
           WHEN i.last_heartbeat_at < ? THEN 'STALE'
           ELSE i.actual_state
       END effective_state,
       i.last_heartbeat_at, i.started_at, i.fencing_token, i.row_version,
       (SELECT GROUP_CONCAT(c.capability_code ORDER BY c.capability_code)
          FROM bat_runtime_capability c
         WHERE c.instance_id = i.instance_id) capabilities
FROM bat_runtime_instance i
ORDER BY i.runtime_role, i.instance_id
