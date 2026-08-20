SELECT approval_id AS approvalId FROM mbw_approval_document
 WHERE request_idempotency_key=:key
