SELECT delegation_id AS delegationId, delegator_employee_no AS delegatorEmployeeNo,
       delegate_employee_no AS delegateEmployeeNo, business_domain AS businessDomain,
       approval_type AS approvalType, valid_from AS validFrom, valid_to AS validTo,
       reason, use_yn AS useYn, created_at AS createdAt, updated_at AS updatedAt
  FROM bza_approval_delegation
 WHERE (:employeeNo IS NULL OR delegator_employee_no=:employeeNo OR delegate_employee_no=:employeeNo)
   AND (:at IS NULL OR (valid_from <= :at AND valid_to > :at))
 ORDER BY valid_from DESC, delegation_id DESC
