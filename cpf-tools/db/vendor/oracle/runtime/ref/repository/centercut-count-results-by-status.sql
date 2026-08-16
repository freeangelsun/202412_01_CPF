SELECT result_status,
       COUNT(*) AS result_count
  FROM ref_center_cut_sample_result
 WHERE center_cut_job_id = ?
 GROUP BY result_status
