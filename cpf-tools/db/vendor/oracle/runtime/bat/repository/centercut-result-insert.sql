INSERT INTO bat_center_cut_result (
    center_cut_item_id,
    center_cut_job_id,
    result_status,
    result_payload,
    result_message,
    transaction_id,
    transaction_segment_id,
    parent_segment_id,
    created_by,
    updated_by
)
SELECT center_cut_item_id,
       center_cut_job_id,
       ?,
       ?,
       ?,
       transaction_id,
       transaction_segment_id,
       parent_segment_id,
       'CENTER_CUT',
       'CENTER_CUT'
  FROM bat_center_cut_item
 WHERE center_cut_item_id = ?
