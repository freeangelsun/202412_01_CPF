SELECT COUNT(*) AS totalCount,
       COALESCE(SUM(CASE WHEN result_status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS successCount,
       COALESCE(SUM(CASE WHEN result_status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failedCount,
       MAX(created_at) AS lastCreatedAt
FROM bat_center_cut_result
WHERE center_cut_job_id = ?
