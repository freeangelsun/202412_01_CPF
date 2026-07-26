UPDATE bat_center_cut_execution
   SET processed_count = processed_count + 1,
       success_count = success_count
           + CASE WHEN ? IN ('SUCCESS', 'COMPLETED') THEN 1 ELSE 0 END,
       unknown_count = unknown_count
           + CASE WHEN ? = 'UNKNOWN_RESULT' THEN 1 ELSE 0 END,
       failure_count = failure_count
           + CASE WHEN ? NOT IN ('SUCCESS', 'COMPLETED', 'UNKNOWN_RESULT') THEN 1 ELSE 0 END,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
