INSERT INTO BAT_CENTER_CUT_EXECUTION(
    center_cut_execution_id, center_cut_job_id, idempotency_key, execution_state,
    parameter_ciphertext, parameter_hash, parameter_schema_version, target_cursor, target_complete_yn,
    target_count, tps_limit, concurrency_limit, transaction_id, parent_segment_id, requested_by,
    reason_text, created_at, updated_at
)
VALUES (?, ?, ?, 'CREATED', ?, ?, ?, NULL, 'N', 0, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
