SELECT unknown_id AS unknownId,
       unknown_type AS unknownType,
       unknown_status AS unknownStatus,
       transaction_global_id AS transactionGlobalId,
       segment_id AS segmentId,
       external_key AS externalKey,
       failure_code AS failureCode,
       failure_message AS failureMessage,
       next_action AS nextAction,
       detected_at AS detectedAt,
       resolved_at AS resolvedAt
FROM cpf_unknown_result
WHERE (? IS NULL OR unknown_type = ?)
  AND (? IS NULL OR unknown_status = ?)
ORDER BY unknown_seq DESC
LIMIT ?
