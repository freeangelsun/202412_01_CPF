UPDATE BAT_DEPLOYMENT_EXECUTION
SET execution_state = ?, failure_stage = ?, result_message = ?,
    reconcile_requested_by = ?, reconcile_approved_by = ?, reconcile_approval_request_id = ?,
    reconcile_reason = ?, reconciled_at = CURRENT_TIMESTAMP(6), finished_at = CURRENT_TIMESTAMP(6)
WHERE deployment_id = ? AND execution_state = 'UNKNOWN_RESULT'
