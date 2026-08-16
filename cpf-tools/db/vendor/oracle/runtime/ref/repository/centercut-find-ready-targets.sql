SELECT target_id,
       center_cut_job_id,
       business_key,
       business_date,
       target_payload,
       transaction_id,
       parent_segment_id,
       transaction_segment_id,
       retry_count,
       status_code
  FROM ref_center_cut_sample_target
 WHERE center_cut_job_id = ?
   AND status_code IN ('READY', 'RETRY_REQUESTED')
   AND use_yn = 'Y'
 ORDER BY business_date ASC, target_id ASC
OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
