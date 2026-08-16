INSERT INTO ref_center_cut_sample_result (
    target_id,
    center_cut_job_id,
    business_key,
    result_status,
    result_payload,
    result_message,
    transaction_id,
    parent_segment_id,
    transaction_segment_id,
    created_by,
    updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'REF_CENTER_CUT', 'REF_CENTER_CUT')
ON CONFLICT (target_id) DO UPDATE SET
    result_status = EXCLUDED.result_status,
    result_payload = EXCLUDED.result_payload,
    result_message = EXCLUDED.result_message,
    transaction_id = EXCLUDED.transaction_id,
    parent_segment_id = EXCLUDED.parent_segment_id,
    transaction_segment_id = EXCLUDED.transaction_segment_id,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP
