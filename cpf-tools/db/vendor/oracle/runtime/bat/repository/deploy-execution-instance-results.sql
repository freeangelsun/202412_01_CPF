SELECT sequence_no, instance_id, stage_code, result_state, result_message
FROM BAT_DEPLOYMENT_INSTANCE_RESULT
WHERE deployment_id = ?
ORDER BY sequence_no
