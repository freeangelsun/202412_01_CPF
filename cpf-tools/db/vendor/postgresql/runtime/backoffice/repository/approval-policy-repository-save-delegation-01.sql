INSERT INTO MBW_APPROVAL_DELEGATION (
    delegator_employee_no, delegate_employee_no, business_domain, approval_type,
    valid_from, valid_to, reason, use_yn, created_by, updated_by
) VALUES (
    :delegatorEmployeeNo, :delegateEmployeeNo, :businessDomain, :approvalType,
    :validFrom, :validTo, :reason, :useYn, :operatorId, :operatorId
)
