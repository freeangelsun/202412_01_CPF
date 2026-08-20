SELECT d.approval_id AS approvalId, d.approval_no AS approvalNo,
       d.approval_type AS approvalType, d.business_domain AS businessDomain,
       d.policy_code AS policyCode, d.policy_version AS policyVersion,
       d.title, d.requester_employee_no AS requesterEmployeeNo,
       d.approval_status AS approvalStatus, d.approval_mode AS approvalMode,
       d.current_step_no AS currentStepNo, d.version_no AS versionNo,
       d.due_at AS dueAt, d.attachment_group_id AS attachmentGroupId,
       d.transaction_id AS transactionId, d.created_at AS createdAt, d.updated_at AS updatedAt,
       CASE WHEN d.approval_status = 'IN_REVIEW' AND EXISTS (
           SELECT 1
             FROM mbw_approval_participant current_participant
            WHERE current_participant.approval_id = d.approval_id
              AND current_participant.approver_employee_no = :employeeNo
              AND current_participant.decision_status = 'WAITING'
              AND (d.approval_mode = 'PARALLEL'
                   OR current_participant.step_no = d.current_step_no)
       ) THEN 'Y' ELSE 'N' END AS actionableYn
  FROM mbw_approval_document d
 WHERE EXISTS (
       SELECT 1
         FROM mbw_approval_participant participant
        WHERE participant.approval_id = d.approval_id
          AND participant.approver_employee_no = :employeeNo
          AND (:decisionStatus IS NULL OR participant.decision_status = :decisionStatus)
 )
 ORDER BY d.approval_id DESC
LIMIT :limit
