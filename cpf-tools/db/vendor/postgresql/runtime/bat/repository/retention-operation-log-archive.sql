INSERT INTO BAT_OPERATION_LOG_ARCHIVE(
  operation_id, job_id, execution_id, operation_type, operator_id, reason, before_data, after_data,
  result_type, result_message, created_by, created_at, updated_by, updated_at,
  archived_at, archived_by, archive_reason
)
SELECT operation_id, job_id, execution_id, operation_type, operator_id, reason, before_data, after_data,
       result_type, result_message, created_by, created_at, updated_by, updated_at, CURRENT_TIMESTAMP, ?, ?
FROM BAT_OPERATION_LOG
WHERE operation_id IN (%s)
