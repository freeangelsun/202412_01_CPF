UPDATE OPS_RETENTION_RUN
SET matched_count = ?, archived_count = ?, deleted_count = ?, processed_count = ?,
    compressed_count = ?, freed_bytes = ?, updated_at = CURRENT_TIMESTAMP
WHERE run_id = ?
