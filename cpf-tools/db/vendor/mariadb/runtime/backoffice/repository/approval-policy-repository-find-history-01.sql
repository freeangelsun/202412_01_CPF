SELECT approval_history_id AS approvalHistoryId,
       approval_id AS approvalId,
       action_type AS actionType,
       actor_employee_no AS actorEmployeeNo,
       idempotency_key AS idempotencyKey,
       reason,
       before_status AS beforeStatus,
       after_status AS afterStatus,
       comment_text AS comment,
       transaction_id AS transactionId,
       created_at AS createdAt
  FROM mbw_approval_history
 WHERE approval_id = :approvalId
 ORDER BY created_at ASC, approval_history_id ASC
