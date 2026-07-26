UPDATE bat_execution
   SET worker_id = NULL,
       updated_at = CURRENT_TIMESTAMP
 WHERE execution_id = ?
