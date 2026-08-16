SELECT COUNT(*) AS totalCount,
       SUM(CASE WHEN status_code = 'READY' THEN 1 ELSE 0 END) AS readyCount,
       SUM(CASE WHEN status_code = 'RUNNING' THEN 1 ELSE 0 END) AS runningCount,
       SUM(CASE WHEN status_code = 'SUCCESS' THEN 1 ELSE 0 END) AS successCount,
       SUM(CASE WHEN status_code = 'FAILED' THEN 1 ELSE 0 END) AS failedCount,
       SUM(CASE WHEN status_code = 'SKIPPED' THEN 1 ELSE 0 END) AS skippedCount,
       SUM(CASE WHEN status_code = 'RETRY_REQUESTED' THEN 1 ELSE 0 END) AS retryRequestedCount,
       SUM(CASE WHEN status_code = 'STOP_REQUESTED' THEN 1 ELSE 0 END) AS stopRequestedCount,
       MAX(started_at) AS lastStartedAt,
       MAX(completed_at) AS lastCompletedAt
  FROM ref_center_cut_sample_target
 WHERE center_cut_job_id = ?
