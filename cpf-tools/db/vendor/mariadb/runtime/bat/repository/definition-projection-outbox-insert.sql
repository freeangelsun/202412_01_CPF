INSERT INTO bat_job_runtime_projection_outbox (
  outbox_id,job_id,definition_version,event_type,payload_hash,event_payload,
  delivery_status,fencing_token,attempt_count,next_attempt_at,created_at
)
VALUES (?,?,?,?,?,?,'PENDING',0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
