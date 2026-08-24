SELECT COUNT(*)
FROM BAT_RUNTIME_INSTANCE
WHERE service_id = ?
  AND actual_state IN ('READY', 'BUSY')
  AND desired_state = 'RUNNING'
