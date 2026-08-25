UPDATE mbw_approval_execution SET execution_status=:status, owner_result_code=:resultCode,
       owner_result_message=:resultMessage, completed_at=CURRENT_TIMESTAMP, recovery_required_yn=:recoveryRequiredYn,
       updated_by=:operatorId
 WHERE approval_id=:approvalId AND fence_token=:fenceToken AND execution_status=:expectedStatus
