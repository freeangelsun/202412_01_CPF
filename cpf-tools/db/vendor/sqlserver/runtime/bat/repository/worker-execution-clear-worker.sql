UPDATE bat_execution
   SET worker_id = NULL,
       updated_at = SYSUTCDATETIME()
 WHERE execution_id = ?
