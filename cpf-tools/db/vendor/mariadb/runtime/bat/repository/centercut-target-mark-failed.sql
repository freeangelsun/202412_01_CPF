UPDATE BAT_CENTER_CUT_EXECUTION
   SET execution_state = 'FAILED',
       last_error_message = ?,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
