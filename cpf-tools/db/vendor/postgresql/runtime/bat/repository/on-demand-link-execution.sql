UPDATE BAT_ON_DEMAND_REQUEST
SET cpf_execution_id = ?, request_status = 'RUNNING', updated_by = ?
WHERE execution_request_id = ? AND request_status = 'REQUESTED'
