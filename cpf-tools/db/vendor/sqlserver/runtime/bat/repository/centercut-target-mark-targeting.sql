UPDATE bat_center_cut_execution
   SET execution_state = 'TARGETING',
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_execution_id = ?
   AND execution_state = 'CREATED'
