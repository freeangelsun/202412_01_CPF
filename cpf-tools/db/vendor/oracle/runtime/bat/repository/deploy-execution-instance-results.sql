SELECT sequence_no, instance_id, stage_code, result_state, result_message
FROM bat_deployment_instance_result
WHERE deployment_id = ?
ORDER BY sequence_no
