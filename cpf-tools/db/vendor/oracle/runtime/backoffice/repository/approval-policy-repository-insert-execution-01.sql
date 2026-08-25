INSERT INTO mbw_approval_execution (approval_id, command_request_id, owner_action, execution_status,
    recovery_required_yn, fence_token, approved_by, transaction_id, created_by, updated_by)
VALUES (:approvalId, :commandRequestId, :ownerAction, 'PENDING', 'N', 0, :approvedBy, :transactionId, :operatorId, :operatorId)
