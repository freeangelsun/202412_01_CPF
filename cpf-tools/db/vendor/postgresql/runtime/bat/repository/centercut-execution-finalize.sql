UPDATE bat_center_cut_execution
   SET execution_state = CASE
           WHEN execution_state = 'CANCELLED' THEN 'CANCELLED'
           WHEN execution_state IN ('PAUSED', 'DRAINING') THEN execution_state
           WHEN unknown_count > 0 THEN 'UNKNOWN_RESULT'
           WHEN failure_count > 0 THEN 'FAILED'
           ELSE 'COMPLETED'
       END,
       completed_at = CASE
           WHEN execution_state IN ('CANCELLED', 'PAUSED', 'DRAINING')
               THEN completed_at
           ELSE CURRENT_TIMESTAMP(6)
       END,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
