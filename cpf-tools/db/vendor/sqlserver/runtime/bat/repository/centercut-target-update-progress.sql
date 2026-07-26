UPDATE bat_center_cut_execution
   SET target_cursor = ?,
       target_complete_yn = ?,
       target_count = target_count + ?,
       execution_state = ?,
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_execution_id = ?
