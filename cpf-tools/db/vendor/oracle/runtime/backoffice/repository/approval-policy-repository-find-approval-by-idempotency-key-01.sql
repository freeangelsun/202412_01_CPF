SELECT approval_id AS approvalId FROM MBW_APPROVAL_DOCUMENT
 WHERE request_idempotency_key=:key
