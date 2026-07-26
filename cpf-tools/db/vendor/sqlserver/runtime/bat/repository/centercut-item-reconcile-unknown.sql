UPDATE bat_center_cut_item
   SET item_status = 'RETRY',
       retry_count = retry_count + 1,
       completed_at = NULL,
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_job_id = ?
   AND item_status = 'UNKNOWN_RESULT'
