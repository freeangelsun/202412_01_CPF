UPDATE bza_approval_line
SET decision_status = :decision, decision_comment = :comment,
    decided_at = CURRENT_TIMESTAMP, updated_by = :actorEmployeeNo, updated_at = CURRENT_TIMESTAMP
WHERE approval_id = :approvalId AND step_no = :stepNo
  AND approver_employee_no = :actorEmployeeNo AND decision_status = 'WAITING'
