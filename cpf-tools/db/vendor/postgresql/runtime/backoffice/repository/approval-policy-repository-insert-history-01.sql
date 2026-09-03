INSERT INTO MBW_APPROVAL_HISTORY (
    approval_id, action_type, actor_employee_no, idempotency_key,
    reason, before_status, after_status, comment_text, transaction_id,
    created_by, updated_by
) VALUES (
    :approvalId, :actionType, :actorEmployeeNo, :idempotencyKey,
    :reason, :beforeStatus, :afterStatus, :comment, :transactionId,
    :operatorId, :operatorId
)
