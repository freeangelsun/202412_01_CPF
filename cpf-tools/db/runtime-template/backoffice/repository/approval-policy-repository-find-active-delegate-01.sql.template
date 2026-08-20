SELECT delegate_employee_no AS delegateEmployeeNo
  FROM mbw_approval_delegation
 WHERE delegator_employee_no=:employeeNo AND use_yn='Y'
   AND valid_from <= :at AND valid_to > :at
   AND (business_domain IS NULL OR business_domain=:businessDomain)
   AND (approval_type IS NULL OR approval_type=:approvalType)
 ORDER BY
   CASE WHEN business_domain IS NULL THEN 1 ELSE 0 END,
   CASE WHEN approval_type IS NULL THEN 1 ELSE 0 END,
   valid_from DESC, delegation_id DESC
