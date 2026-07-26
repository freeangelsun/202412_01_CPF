UPDATE bat_execution
   SET execution_status = 'CLAIMED',
       worker_id = ?,
       last_heartbeat_at = ?
 WHERE execution_id = ?
   AND execution_status = 'CLAIMING'
