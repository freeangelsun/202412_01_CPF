SELECT approval_history_id AS historyId, action_type AS actionType,
       actor_employee_no AS actorEmployeeNo, reason, before_status AS beforeStatus,
       after_status AS afterStatus, comment_text AS comment,
       transaction_id AS transactionId, created_at AS createdAt
  FROM bza_approval_history
 WHERE approval_id = :approvalId ORDER BY approval_history_id
