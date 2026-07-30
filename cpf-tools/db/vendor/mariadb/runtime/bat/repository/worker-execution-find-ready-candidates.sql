SELECT execution_id,
       required_worker_version,
       required_capability
  FROM bat_execution
 WHERE execution_status = 'READY'
   AND stop_requested_yn = 'N'
   AND definition_version IS NOT NULL
   AND definition_checksum IS NOT NULL
 ORDER BY execution_id
 LIMIT 100
