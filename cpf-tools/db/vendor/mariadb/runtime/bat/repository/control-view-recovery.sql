SELECT 'EXECUTION' type, CAST(execution_id AS CHAR) id, execution_status state, error_message detail
FROM bat_execution
WHERE execution_status = 'UNKNOWN_RESULT'
UNION ALL
SELECT 'CENTER_CUT', CAST(center_cut_item_id AS CHAR), item_status, last_error_message
FROM bat_center_cut_item
WHERE item_status = 'UNKNOWN_RESULT'
UNION ALL
SELECT 'COMMAND', command_id, command_state, result_text
FROM bat_runtime_command
WHERE command_state = 'UNKNOWN_RESULT'
