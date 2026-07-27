INSERT INTO bza_approval_line (
    approval_id, step_no, approver_employee_no,
    step_type, target_type, target_code, decision_rule, required_yn,
    decision_status, created_by, updated_by
) VALUES (
    :approvalId, :stepNo, :approverEmployeeNo,
    'APPROVAL', 'EMPLOYEE', :approverEmployeeNo, :decisionRule, 'Y',
    'WAITING', :requestUser, :requestUser
)
