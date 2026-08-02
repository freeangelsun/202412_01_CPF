SELECT COUNT(*) AS totalCount,
       SUM(CASE WHEN result_status = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
       SUM(CASE WHEN result_status = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
       MAX(created_at) AS lastCreatedAt
  FROM ref_center_cut_sample_result
 WHERE center_cut_job_id = ?
