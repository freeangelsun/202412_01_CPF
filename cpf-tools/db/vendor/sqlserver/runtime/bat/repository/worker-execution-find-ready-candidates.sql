SELECT execution_id,
       required_worker_version,
       required_capability
  FROM bat_execution
 WHERE execution_status = 'READY'
   AND stop_requested_yn = 'N'
 ORDER BY execution_id
 OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY
