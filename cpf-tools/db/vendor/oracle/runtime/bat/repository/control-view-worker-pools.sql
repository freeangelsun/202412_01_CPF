SELECT r.pool_id, r.artifact_version, COUNT(*) instances,
       SUM(CASE WHEN r.actual_state IN ('READY', 'BUSY') THEN 1 ELSE 0 END) healthy,
       SUM(CASE WHEN r.actual_state = 'DRAINING' THEN 1 ELSE 0 END) draining
FROM bat_runtime_instance r
WHERE r.runtime_role = 'WORKER'
GROUP BY r.pool_id, r.artifact_version
ORDER BY r.pool_id, r.artifact_version
