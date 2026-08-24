INSERT INTO BAT_CENTER_CUT_ITEM (
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
)
SELECT center_cut_job_id,
       ?,
       ?,
       'READY',
       transaction_id,
       ?,
       parent_segment_id,
       ?,
       'CENTER_CUT_TARGET',
       'CENTER_CUT_TARGET'
  FROM BAT_CENTER_CUT_EXECUTION
 WHERE center_cut_execution_id = ?
ON CONFLICT (center_cut_execution_id, business_key) DO NOTHING
