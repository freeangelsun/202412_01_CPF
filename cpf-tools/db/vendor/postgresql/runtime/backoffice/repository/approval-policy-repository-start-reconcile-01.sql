UPDATE MBW_APPROVAL_EXECUTION SET execution_status='RECONCILING', completed_at=NULL,
       fence_token=fence_token+1, updated_by=:operatorId
 WHERE approval_id=:approvalId AND execution_status='UNKNOWN' AND fence_token=:expectedFence
