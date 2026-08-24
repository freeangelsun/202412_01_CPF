SELECT request_id, entity_key, from_status, to_status, requester_id, approver_id,
       reason_text, idempotency_key, expected_attempt, created_at
  FROM BAT_RECONCILIATION_AUDIT
 WHERE idempotency_key = ?
   AND entity_type = 'SCHEDULER_TRIGGER'
