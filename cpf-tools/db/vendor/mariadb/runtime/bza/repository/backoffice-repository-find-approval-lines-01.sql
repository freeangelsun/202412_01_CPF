SELECT approval_line_id AS approvalLineId, approval_id AS approvalId, step_no AS stepNo,
       approver_employee_no AS approverEmployeeNo, decision_rule AS decisionRule,
       decision_status AS decisionStatus, delegated_from_employee_no AS delegatedFromEmployeeNo,
       decision_comment AS decisionComment, decided_at AS decidedAt
  FROM bza_approval_line
 WHERE approval_id = :approvalId
 ORDER BY step_no, approval_line_id
