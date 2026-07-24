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
FROM cpf_idempotency_record
WHERE scope = ?
  AND idempotency_key = ?
ORDER BY idempotency_id
OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY
