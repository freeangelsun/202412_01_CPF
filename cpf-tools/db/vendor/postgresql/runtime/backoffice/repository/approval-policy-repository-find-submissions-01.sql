SELECT approval_id AS approvalId, approval_no AS approvalNo,
       approval_type AS approvalType, business_domain AS businessDomain,
       policy_code AS policyCode, policy_version AS policyVersion,
       title, requester_employee_no AS requesterEmployeeNo,
       approval_status AS approvalStatus, approval_mode AS approvalMode,
       current_step_no AS currentStepNo, version_no AS versionNo,
       due_at AS dueAt, attachment_group_id AS attachmentGroupId,
       resubmitted_from_approval_id AS resubmittedFromApprovalId,
       transaction_id AS transactionId, created_at AS createdAt, updated_at AS updatedAt
  FROM mbw_approval_document
 WHERE requester_employee_no = :employeeNo
   AND (:status IS NULL OR approval_status = :status)
 ORDER BY approval_id DESC
LIMIT :limit
