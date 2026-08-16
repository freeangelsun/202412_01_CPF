UPDATE bat_center_cut_execution
   SET target_cursor = ?,
       target_complete_yn = ?,
       target_count = target_count + ?,
       execution_state = ?,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
