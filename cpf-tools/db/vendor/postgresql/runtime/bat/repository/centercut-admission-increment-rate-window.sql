UPDATE bat_center_cut_rate_window
   SET admitted_count = admitted_count + 1,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_execution_id = ?
   AND window_second = ?
   AND admitted_count < ?
