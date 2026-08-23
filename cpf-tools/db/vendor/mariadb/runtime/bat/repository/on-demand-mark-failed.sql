UPDATE bat_on_demand_request
SET request_status = 'FAILED', failure_code = ?, failure_message = ?, completed_at = {{NOW3}}, updated_by = ?
WHERE execution_request_id = ?
