INSERT INTO BAT_ON_DEMAND_REQUEST(
 execution_request_id, standard_batch_id, idempotency_key, transaction_id, business_date,
 request_status, parameters_json, request_reason, request_user, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, 'REQUESTED', ?, ?, ?, ?, ?)
