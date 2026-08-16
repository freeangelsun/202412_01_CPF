SELECT i.center_cut_job_id, i.item_status, COUNT(*) count, MAX(i.updated_at) last_updated
FROM bat_center_cut_item i
GROUP BY i.center_cut_job_id, i.item_status
ORDER BY i.center_cut_job_id, i.item_status
