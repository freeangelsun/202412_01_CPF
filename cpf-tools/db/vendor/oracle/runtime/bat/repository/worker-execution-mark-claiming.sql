UPDATE bat_execution
   SET execution_status = 'CLAIMING',
       worker_id = ?,
       last_heartbeat_at = ?
 WHERE execution_id = ?
   AND execution_status = 'READY'
   AND stop_requested_yn = 'N'
