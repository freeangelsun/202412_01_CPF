UPDATE bat_on_demand_request
SET cpf_execution_id = ?, request_status = 'RUNNING', updated_by = ?
WHERE execution_request_id = ? AND request_status = 'REQUESTED'
