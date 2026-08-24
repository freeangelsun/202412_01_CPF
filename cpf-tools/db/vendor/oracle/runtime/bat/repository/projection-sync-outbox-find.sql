SELECT outbox_id,job_id,definition_version,event_type,payload_hash,event_payload,attempt_count
FROM BAT_JOB_RUNTIME_PROJECTION_OUTBOX
WHERE delivery_status IN ('PENDING','RETRY')
  AND (next_attempt_at IS NULL OR next_attempt_at <= CURRENT_TIMESTAMP)
  AND (lease_until IS NULL OR lease_until < CURRENT_TIMESTAMP)
ORDER BY created_at
