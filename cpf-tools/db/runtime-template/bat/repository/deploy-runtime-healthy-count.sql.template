SELECT COUNT(*)
FROM bat_runtime_instance
WHERE service_id = ?
  AND actual_state IN ('READY', 'BUSY')
  AND desired_state = 'RUNNING'
