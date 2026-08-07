SELECT center_cut_job_id, execution_state,
       failure_count AS failed_count,
       unknown_count
FROM bat_center_cut_execution
WHERE center_cut_execution_id = ?
