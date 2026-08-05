WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET ECHO OFF
SET VERIFY OFF
SET DEFINE OFF
INSERT INTO bat_operation_log_archive (operation_id, job_id, execution_id, operation_type, operator_id, reason, before_data, after_data, result_type, result_message, created_by, created_at, updated_by, updated_at, archived_at, archived_by, archive_reason)
SELECT s.operation_id, s.job_id, s.execution_id, s.operation_type, s.operator_id, s.reason, s.before_data, s.after_data, s.result_type, s.result_message, s.created_by, s.created_at, s.updated_by, s.updated_at, CURRENT_TIMESTAMP, 'devgpt-v9-s04', 'S04 deterministic retention lifecycle verification'
FROM bat_operation_log s WHERE s.operation_id IN (SELECT s.operation_id FROM bat_operation_log s WHERE s.created_at < TO_TIMESTAMP_TZ('2026-05-07T00:00:00+00:00', 'YYYY-MM-DD"T"HH24:MI:SSTZH:TZM') AND 1=1 ORDER BY s.created_at, s.operation_id FETCH FIRST 10000 ROWS ONLY)
AND NOT EXISTS (SELECT 1 FROM bat_operation_log_archive a WHERE a.operation_id=s.operation_id);
DELETE FROM bat_operation_log s WHERE s.operation_id IN (SELECT s.operation_id FROM bat_operation_log s WHERE s.created_at < TO_TIMESTAMP_TZ('2026-05-07T00:00:00+00:00', 'YYYY-MM-DD"T"HH24:MI:SSTZH:TZM') AND 1=1 ORDER BY s.created_at, s.operation_id FETCH FIRST 10000 ROWS ONLY)
AND EXISTS (SELECT 1 FROM bat_operation_log_archive a WHERE a.operation_id=s.operation_id);
COMMIT;
EXIT
