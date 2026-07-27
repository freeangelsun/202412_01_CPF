INSERT INTO bza_approval_document (
    approval_no, approval_type, business_domain, title, requester_employee_no,
    approval_status, approval_mode, current_step_no, due_at, payload_json,
    attachment_group_id, version_no, transaction_id, created_by, updated_by
) VALUES (
    :approvalNo, :approvalType, :businessDomain, :title, :requesterEmployeeNo,
    'DRAFT', :approvalMode, 0, :dueAt, :payloadJson,
    :attachmentGroupId, 0, :transactionId, :requestUser, :requestUser
)
