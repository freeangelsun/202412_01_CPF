INSERT INTO BAT_OPERATION_LOG(
    job_id, operation_type, operator_id, reason, before_data, after_data,
    result_type, result_message, created_by, updated_by
)
VALUES (?, 'RECONCILE_REMOTE_MESSAGE', ?, ?, ?, ?, 'S', ?, ?, ?)
