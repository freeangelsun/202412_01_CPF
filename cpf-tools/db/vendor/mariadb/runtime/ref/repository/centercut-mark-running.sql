UPDATE ref_center_cut_sample_target
   SET status_code = 'RUNNING',
       transaction_id = ?,
       parent_segment_id = ?,
       transaction_segment_id = ?,
       started_at = CURRENT_TIMESTAMP,
       updated_by = 'REF_CENTER_CUT',
       updated_at = CURRENT_TIMESTAMP
 WHERE target_id = ?
   AND center_cut_job_id = ?
   AND status_code IN ('READY', 'RETRY_REQUESTED')
