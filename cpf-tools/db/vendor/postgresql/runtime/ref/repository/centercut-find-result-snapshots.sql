SELECT target_id,
       business_key,
       result_status,
       result_message,
       transaction_id,
       parent_segment_id,
       transaction_segment_id
  FROM ref_center_cut_sample_result
 WHERE center_cut_job_id = ?
 ORDER BY target_id
