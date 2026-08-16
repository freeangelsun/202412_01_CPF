INSERT INTO bat_operation_log_archive(
    operation_id, job_id, execution_id, operation_type, operator_id, reason,
    before_data, after_data, result_type, result_message, created_by, created_at,
    updated_by, updated_at, archived_at, archived_by, archive_reason
)
SELECT l.operation_id, l.job_id, l.execution_id, l.operation_type, l.operator_id, l.reason,
       l.before_data, l.after_data, l.result_type, l.result_message, l.created_by, l.created_at,
       l.updated_by, l.updated_at, CURRENT_TIMESTAMP(3), ?, ?
FROM bat_operation_log l
WHERE l.created_at < ?
