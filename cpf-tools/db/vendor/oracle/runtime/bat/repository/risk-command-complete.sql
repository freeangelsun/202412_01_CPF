UPDATE BAT_OPERATION_REQUEST
SET request_state = ?, result_payload = ?, failure_code = ?, failure_message = ?,
    completed_at = CURRENT_TIMESTAMP, updated_by = ?, updated_at = CURRENT_TIMESTAMP
WHERE idempotency_key = ? AND request_hash = ? AND request_state IN ('RESERVED', 'UNKNOWN')
