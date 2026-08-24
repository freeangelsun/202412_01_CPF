INSERT INTO BAT_JOB_DEFINITION_AUDIT (
  job_id,definition_version,action_code,from_state,to_state,reason,operator_id,
  requested_by,approval_request_id,transaction_id,trace_id,before_json,after_json,created_at
)
VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
