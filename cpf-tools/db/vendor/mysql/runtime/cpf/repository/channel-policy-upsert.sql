INSERT INTO cpf_channel_execution_policy (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code,
    request_type, allowed_yn, authentication_required_yn, signature_required_yn,
    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id),
    original_channel_code = VALUES(original_channel_code),
    caller_channel_code = VALUES(caller_channel_code),
    request_type = VALUES(request_type),
    allowed_yn = VALUES(allowed_yn),
    authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn),
    max_tps = VALUES(max_tps),
    effective_from = VALUES(effective_from),
    effective_to = VALUES(effective_to),
    active_yn = VALUES(active_yn),
    policy_version = VALUES(policy_version),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP
