SELECT approval_id AS approvalId, approval_no AS approvalNo,
       approval_type AS approvalType, business_domain AS businessDomain,
       policy_code AS policyCode, policy_version AS policyVersion,
       title, requester_employee_no AS requesterEmployeeNo,
       approval_status AS approvalStatus, approval_mode AS approvalMode,
       current_step_no AS currentStepNo, version_no AS versionNo,
       due_at AS dueAt, payload_json AS payloadJson, payload_hash AS payloadHash,
       attachment_group_id AS attachmentGroupId,
       resubmitted_from_approval_id AS resubmittedFromApprovalId, transaction_id AS transactionId
  FROM bza_approval_document WHERE approval_id=:approvalId
