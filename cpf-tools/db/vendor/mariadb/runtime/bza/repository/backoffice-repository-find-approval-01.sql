SELECT approval_id AS approvalId, approval_no AS approvalNo, approval_type AS approvalType,
       business_domain AS businessDomain, title, requester_employee_no AS requesterEmployeeNo,
       approval_status AS approvalStatus, approval_mode AS approvalMode,
       current_step_no AS currentStepNo, due_at AS dueAt, payload_json AS payloadJson,
       attachment_group_id AS attachmentGroupId, version_no AS versionNo,
       transaction_id AS transactionId, created_at AS createdAt, updated_at AS updatedAt
  FROM bza_approval_document WHERE approval_id = :approvalId
