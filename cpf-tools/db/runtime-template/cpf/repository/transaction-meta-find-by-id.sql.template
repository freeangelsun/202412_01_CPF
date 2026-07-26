SELECT transaction_id, transaction_name, module_code, domain_code, http_method, api_path,
       controller_class, handler_method, swagger_operation_id, log_policy_key,
       sensitive_yn, masking_policy_key, active_yn, first_detected_at, last_detected_at,
       last_scanned_at, created_by, created_at, updated_by, updated_at
FROM cpf_transaction_meta
WHERE transaction_id = ?
