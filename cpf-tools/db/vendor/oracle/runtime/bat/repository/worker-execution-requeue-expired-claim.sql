UPDATE bat_execution
   SET execution_status = 'READY',
       worker_id = NULL,
       last_heartbeat_at = NULL,
       error_message = NULL,
       updated_at = CURRENT_TIMESTAMP
 WHERE execution_id = ?
   AND execution_status IN ('CLAIMING', 'CLAIMED')
