INSERT INTO MBW_APPROVAL_LINE (
    approval_id, step_no, approver_employee_no, step_type, target_type, target_code,
    target_name_snapshot, decision_rule, required_count, required_yn,
    decision_status, created_by, updated_by
) VALUES (
    :approvalId, :stepNo, :directApprover, :stepType, :targetType, :targetCode,
    :targetName, :decisionRule, :requiredCount, :requiredYn,
    'WAITING', :operatorId, :operatorId
)
