SELECT system_id, instance_id, version, build_sha, started_at, uptime_ms,
       last_seen_at, liveness, readiness, startup, draining, maintenance, payload_json
  FROM cpf_runtime_instance_health
 WHERE system_id = :system_id
 ORDER BY last_seen_at DESC, instance_id;
