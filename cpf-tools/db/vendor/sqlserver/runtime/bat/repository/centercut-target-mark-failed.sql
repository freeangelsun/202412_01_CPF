UPDATE bat_center_cut_execution
   SET execution_state = 'FAILED',
       last_error_message = ?,
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_execution_id = ?
