INSERT INTO bat_reconciliation_audit
       (request_id, entity_type, entity_key, from_status, to_status, requester_id,
        approver_id, reason_text, idempotency_key, expected_attempt, expected_version, created_at)
VALUES (?, 'REMOTE_MESSAGE', CONCAT(?, ':', ?), 'UNKNOWN', 'FAILED', ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))
