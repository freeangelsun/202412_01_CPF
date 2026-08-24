UPDATE BAT_CENTER_CUT_EXECUTION
   SET target_cursor = ?,
       target_complete_yn = ?,
       target_count = target_count + ?,
       execution_state = CASE
           WHEN execution_state = 'STARTING' AND ? = 'Y' THEN 'RUNNING'
           WHEN execution_state = 'STARTING' THEN 'STARTING'
           ELSE ?
       END,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
