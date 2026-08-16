SELECT request_id, entity_key, from_status, to_status, requester_id, approver_id,
       reason_text, idempotency_key, expected_attempt, created_at
  FROM bat_reconciliation_audit
 WHERE idempotency_key = ?
   AND entity_type = 'SCHEDULER_TRIGGER'
