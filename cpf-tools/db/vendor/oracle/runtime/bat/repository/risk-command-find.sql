SELECT idempotency_key, request_hash, request_state, result_payload, failure_code,
       failure_message, updated_at
FROM BAT_OPERATION_REQUEST
WHERE idempotency_key = ?
