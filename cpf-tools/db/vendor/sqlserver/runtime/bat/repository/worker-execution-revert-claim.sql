UPDATE bat_execution
   SET execution_status = 'READY',
       worker_id = NULL
 WHERE execution_id = ?
   AND execution_status = 'CLAIMING'
