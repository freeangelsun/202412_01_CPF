SELECT e.center_cut_execution_id,
       e.center_cut_job_id,
       e.parameter_ciphertext,
       e.parameter_hash,
       e.target_cursor,
       e.transaction_id,
       e.parent_segment_id,
       j.provider_key,
       j.chunk_size
  FROM bat_center_cut_execution e
  JOIN bat_center_cut_job j
    ON j.center_cut_job_id = e.center_cut_job_id
 WHERE e.execution_state IN ('CREATED', 'TARGETING')
   AND e.target_complete_yn = 'N'
 ORDER BY e.created_at
 LIMIT 20
