SELECT p.approval_participant_id AS participantId, p.approval_line_id AS lineId,
       p.step_no AS stepNo, p.decision_status AS decisionStatus,
       l.step_type AS stepType, l.decision_rule AS decisionRule,
       l.required_count AS requiredCount, l.required_yn AS requiredYn
  FROM MBW_APPROVAL_PARTICIPANT p
  JOIN MBW_APPROVAL_LINE l ON l.approval_line_id=p.approval_line_id
 WHERE p.approval_id=:approvalId AND p.approver_employee_no=:employeeNo
   AND p.decision_status='WAITING'
 ORDER BY p.step_no, p.approval_participant_id
