INSERT INTO bza_approval_policy_step (
    policy_code, policy_version, step_no, step_type, target_type, target_code,
    decision_rule, required_count, required_yn, sort_order, created_by, updated_by
) VALUES (
    :policyCode, :policyVersion, :stepNo, :stepType, :targetType, :targetCode,
    :decisionRule, :requiredCount, :requiredYn, :sortOrder, :operatorId, :operatorId
)
