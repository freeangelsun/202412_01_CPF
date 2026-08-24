INSERT INTO BAT_RECONCILIATION_AUDIT (
       request_id, entity_type, entity_key, from_status, to_status,
       requester_id, approver_id, reason_text, idempotency_key,
       expected_attempt, expected_version, created_at)
VALUES (?, 'SCHEDULER_TRIGGER', ?, 'UNKNOWN', 'FAILED', ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP(6))
