SELECT approval_id AS approvalId, command_request_id AS commandRequestId, owner_action AS ownerAction,
       execution_status AS executionStatus, owner_result_code AS ownerResultCode,
       owner_result_message AS ownerResultMessage, started_at AS startedAt, completed_at AS completedAt,
       recovery_required_yn AS recoveryRequiredYn, fence_token AS fenceToken, approved_by AS approvedBy,
       transaction_id AS transactionId, created_at AS createdAt, updated_at AS updatedAt
  FROM mbw_approval_execution WHERE approval_id=:approvalId
