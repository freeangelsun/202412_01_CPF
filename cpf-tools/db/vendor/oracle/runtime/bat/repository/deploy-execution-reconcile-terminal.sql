UPDATE bat_deployment_execution
SET execution_state = ?, failure_stage = ?, result_message = ?,
    reconcile_requested_by = ?, reconcile_approved_by = ?, reconcile_approval_request_id = ?,
    reconcile_reason = ?, reconciled_at = SYSTIMESTAMP, finished_at = SYSTIMESTAMP
WHERE deployment_id = ? AND execution_state = 'UNKNOWN_RESULT'
