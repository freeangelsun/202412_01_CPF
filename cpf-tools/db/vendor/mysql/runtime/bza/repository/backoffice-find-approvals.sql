SELECT approval_id AS approvalId, approval_no AS approvalNo, approval_type AS approvalType,
       business_domain AS businessDomain, title, requester_employee_no AS requesterEmployeeNo,
       approval_status AS approvalStatus, approval_mode AS approvalMode,
       current_step_no AS currentStepNo, due_at AS dueAt, version_no AS versionNo,
       transaction_global_id AS transactionGlobalId, created_at AS createdAt, updated_at AS updatedAt
FROM bza_approval_document
WHERE (:status IS NULL OR approval_status = :status)
  AND (:employeeNo IS NULL OR requester_employee_no = :employeeNo
       OR EXISTS (SELECT 1 FROM bza_approval_line l
                  WHERE l.approval_id = bza_approval_document.approval_id
                    AND l.approver_employee_no = :employeeNo))
ORDER BY approval_id DESC
LIMIT :limit
