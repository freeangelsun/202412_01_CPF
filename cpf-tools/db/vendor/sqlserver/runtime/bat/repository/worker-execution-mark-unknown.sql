UPDATE bat_execution
   SET execution_status = 'UNKNOWN_RESULT',
       worker_id = NULL,
       end_time = SYSUTCDATETIME(),
       error_message = 'Worker lease expired after execution may have started; reconcile before retry',
       updated_at = SYSUTCDATETIME()
 WHERE execution_id = ?
   AND execution_status IN ('RUNNING', 'CLAIMING', 'CLAIMED')
