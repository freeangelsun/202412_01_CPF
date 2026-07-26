SELECT i.center_cut_item_id,
       i.center_cut_execution_id,
       i.business_key,
       i.item_payload,
       i.center_cut_job_id,
       i.transaction_id,
       i.transaction_segment_id,
       j.handler_key,
       COALESCE(j.batch_job_id, j.center_cut_job_id) AS job_code
  FROM bat_center_cut_item i
  JOIN bat_center_cut_job j
    ON j.center_cut_job_id = i.center_cut_job_id
 WHERE i.center_cut_item_id = ?
