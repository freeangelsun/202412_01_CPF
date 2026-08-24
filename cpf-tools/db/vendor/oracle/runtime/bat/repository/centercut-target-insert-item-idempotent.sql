MERGE INTO BAT_CENTER_CUT_ITEM target
USING (
    SELECT e.center_cut_job_id,
           ? AS center_cut_execution_id,
           ? AS business_key,
           e.transaction_id,
           ? AS transaction_segment_id,
           e.parent_segment_id,
           ? AS item_payload
      FROM BAT_CENTER_CUT_EXECUTION e
     WHERE e.center_cut_execution_id = ?
) source
ON (
    target.center_cut_execution_id = source.center_cut_execution_id
    AND target.business_key = source.business_key
)
WHEN NOT MATCHED THEN INSERT (
    center_cut_job_id,
    center_cut_execution_id,
    business_key,
    item_status,
    transaction_id,
    transaction_segment_id,
    parent_segment_id,
    item_payload,
    created_by,
    updated_by
) VALUES (
    source.center_cut_job_id,
    source.center_cut_execution_id,
    source.business_key,
    'READY',
    source.transaction_id,
    source.transaction_segment_id,
    source.parent_segment_id,
    source.item_payload,
    'CENTER_CUT_TARGET',
    'CENTER_CUT_TARGET'
)
