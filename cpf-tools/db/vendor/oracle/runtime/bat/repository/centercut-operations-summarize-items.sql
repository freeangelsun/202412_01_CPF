SELECT COUNT(*) AS totalCount,
       COALESCE(SUM(CASE WHEN item_status = 'READY' THEN 1 ELSE 0 END), 0) AS readyCount,
       COALESCE(SUM(CASE WHEN item_status = 'RUNNING' THEN 1 ELSE 0 END), 0) AS runningCount,
       COALESCE(SUM(CASE WHEN item_status = 'SUCCESS' THEN 1 ELSE 0 END), 0) AS successCount,
       COALESCE(SUM(CASE WHEN item_status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failedCount,
       COALESCE(SUM(CASE WHEN item_status = 'SKIPPED' THEN 1 ELSE 0 END), 0) AS skippedCount,
       COALESCE(SUM(CASE WHEN item_status = 'RETRY_REQUESTED' THEN 1 ELSE 0 END), 0) AS retryRequestedCount,
       COALESCE(SUM(CASE WHEN item_status = 'STOP_REQUESTED' THEN 1 ELSE 0 END), 0) AS stopRequestedCount,
       MAX(started_at) AS lastStartedAt,
       MAX(completed_at) AS lastCompletedAt
FROM bat_center_cut_item
WHERE center_cut_job_id = ?
