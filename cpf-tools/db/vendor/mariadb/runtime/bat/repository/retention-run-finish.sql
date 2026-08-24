UPDATE OPS_RETENTION_RUN
SET status = ?, completed_at = CURRENT_TIMESTAMP, error_code = ?, error_summary = ?, updated_at = CURRENT_TIMESTAMP
WHERE run_id = ?
