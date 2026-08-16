UPDATE ref_center_cut_sample_target
   SET status_code = ?,
       transaction_id = ?,
       parent_segment_id = ?,
       transaction_segment_id = ?,
       completed_at = CURRENT_TIMESTAMP,
       last_error_message = ?,
       updated_by = 'REF_CENTER_CUT',
       updated_at = CURRENT_TIMESTAMP
 WHERE target_id = ?
   AND center_cut_job_id = ?
