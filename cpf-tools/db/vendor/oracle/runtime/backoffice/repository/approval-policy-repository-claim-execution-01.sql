UPDATE MBW_APPROVAL_EXECUTION SET execution_status='RUNNING', started_at=CURRENT_TIMESTAMP, completed_at=NULL,
       owner_result_code=NULL, owner_result_message=NULL, recovery_required_yn='N', fence_token=fence_token+1, updated_by=:operatorId
 WHERE approval_id=:approvalId AND execution_status='PENDING' AND fence_token=:expectedFence
