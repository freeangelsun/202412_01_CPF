SELECT approval_line_id AS lineId, step_no AS stepNo, step_type AS stepType,
       decision_rule AS decisionRule, required_count AS requiredCount,
       required_yn AS requiredYn, decision_status AS decisionStatus
  FROM bza_approval_line
 WHERE approval_id=:approvalId
 ORDER BY step_no, approval_line_id
