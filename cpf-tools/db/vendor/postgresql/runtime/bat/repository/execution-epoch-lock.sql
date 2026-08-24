SELECT current_fencing_token
  FROM BAT_EXECUTION_EPOCH
 WHERE job_id = ?
 FOR UPDATE
