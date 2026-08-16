MERGE INTO ref_center_cut_sample_result target
USING (
    SELECT ? AS target_id,
           ? AS center_cut_job_id,
           ? AS business_key,
           ? AS result_status,
           ? AS result_payload,
           ? AS result_message,
           ? AS transaction_id,
           ? AS parent_segment_id,
           ? AS transaction_segment_id
      FROM dual
) source
ON (target.target_id = source.target_id)
WHEN MATCHED THEN UPDATE SET
    target.result_status = source.result_status,
    target.result_payload = source.result_payload,
    target.result_message = source.result_message,
    target.transaction_id = source.transaction_id,
    target.parent_segment_id = source.parent_segment_id,
    target.transaction_segment_id = source.transaction_segment_id,
    target.updated_by = 'REF_CENTER_CUT',
    target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
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
) VALUES (
    source.target_id,
    source.center_cut_job_id,
    source.business_key,
    source.result_status,
    source.result_payload,
    source.result_message,
    source.transaction_id,
    source.parent_segment_id,
    source.transaction_segment_id,
    'REF_CENTER_CUT',
    'REF_CENTER_CUT'
)
