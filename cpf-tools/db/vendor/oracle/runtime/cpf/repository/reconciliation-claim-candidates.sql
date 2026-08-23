SELECT unknown_id AS unknownId,
       unknown_type AS unknownType,
       unknown_status AS unknownStatus,
       transaction_id AS transactionId,
       segment_id AS segmentId,
       external_key AS externalKey,
       failure_code AS failureCode,
       failure_message AS failureMessage,
       next_action AS nextAction,
       detected_at AS detectedAt,
       resolved_at AS resolvedAt,
       attempt_count AS attemptCount,
       row_version AS rowVersion
FROM cpf_unknown_result
WHERE unknown_status = 'CHECK_PENDING'
  AND (? IS NULL OR unknown_type = ?)
  AND detected_at <= ?
  AND (next_check_at IS NULL OR next_check_at <= ?)
  AND (lease_until IS NULL OR lease_until < ?)
ORDER BY detected_at, unknown_seq
