SELECT approval_id AS approvalId FROM bza_approval_document
 WHERE request_idempotency_key=:key
