SELECT r.execution_request_id, r.standard_batch_id, r.idempotency_key, r.transaction_id, r.business_date,
 r.request_status, r.cpf_execution_id, r.spring_batch_execution_id, r.failure_code, r.failure_message, e.job_id
FROM bat_on_demand_request r
LEFT JOIN bat_execution e ON e.execution_id = r.cpf_execution_id
WHERE r.execution_request_id = ?
