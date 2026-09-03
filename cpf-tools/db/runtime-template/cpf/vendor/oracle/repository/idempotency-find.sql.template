SELECT scope,
       idempotency_key AS idempotencyKey,
       request_hash AS requestHash,
       payload_hash AS payloadHash,
       record_status AS recordStatus,
       stored_response AS storedResponse,
       retry_allowed_yn AS retryAllowedYn,
       created_at AS createdAt,
       completed_at AS completedAt,
       expires_at AS expiresAt
FROM CPF_IDEMPOTENCY_RECORD
WHERE scope = ?
  AND idempotency_key = ?
FETCH FIRST 1 ROW ONLY
