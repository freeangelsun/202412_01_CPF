SELECT p.approval_participant_id AS approvalParticipantId, p.approval_id AS approvalId,
       p.approval_line_id AS approvalLineId, p.step_no AS stepNo,
       p.approver_employee_no AS approverEmployeeNo,
       p.approver_name_snapshot AS approverNameSnapshot,
       p.organization_code_snapshot AS organizationCodeSnapshot,
       p.position_code_snapshot AS positionCodeSnapshot,
       p.job_title_code_snapshot AS jobTitleCodeSnapshot,
       p.delegated_from_employee_no AS delegatedFromEmployeeNo,
       p.resolution_source AS resolutionSource, p.decision_status AS decisionStatus,
       p.decision_comment AS decisionComment, p.decided_at AS decidedAt,
       l.step_type AS stepType, l.decision_rule AS decisionRule,
       l.required_count AS requiredCount, l.required_yn AS requiredYn
  FROM MBW_APPROVAL_PARTICIPANT p
  JOIN MBW_APPROVAL_LINE l ON l.approval_line_id=p.approval_line_id
 WHERE p.approval_id=:approvalId
 ORDER BY p.step_no, p.approval_line_id, p.approval_participant_id
