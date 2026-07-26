SELECT execution_id,
       job_id,
       job_parameters,
       transaction_id,
       transaction_segment_id,
       business_date,
       requested_by
  FROM bat_execution
 WHERE execution_id = ?
