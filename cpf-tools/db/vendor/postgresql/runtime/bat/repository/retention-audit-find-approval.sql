SELECT audit_id AS auditId, operation_type AS operationType, target_type AS targetType,
       target_id AS targetId, requested_by AS requestedBy, approved_by AS approvedBy,
       approval_request_id AS approvalRequestId, reason_text AS reason,
       expected_version AS expectedVersion, result_state AS resultState, created_at AS createdAt
FROM OPS_RETENTION_CONTROL_AUDIT
WHERE approval_request_id = ?
ORDER BY created_at, audit_id
