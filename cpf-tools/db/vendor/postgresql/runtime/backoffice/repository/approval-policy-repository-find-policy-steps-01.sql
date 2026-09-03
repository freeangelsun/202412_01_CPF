SELECT policy_code AS policyCode, policy_version AS policyVersion,
       step_no AS stepNo, step_type AS stepType,
       target_type AS targetType, target_code AS targetCode,
       decision_rule AS decisionRule, required_count AS requiredCount,
       required_yn AS requiredYn, sort_order AS sortOrder
  FROM MBW_APPROVAL_POLICY_STEP
 WHERE policy_code = :policyCode AND policy_version = :policyVersion
 ORDER BY step_no, sort_order, target_type, target_code
