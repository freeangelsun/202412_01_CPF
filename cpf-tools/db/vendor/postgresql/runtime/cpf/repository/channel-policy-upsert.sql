INSERT INTO cpf_channel_execution_policy (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code,
    request_type, allowed_yn, authentication_required_yn, signature_required_yn,
    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (policy_key) DO UPDATE SET
    standard_execution_id = EXCLUDED.standard_execution_id,
    original_channel_code = EXCLUDED.original_channel_code,
    caller_channel_code = EXCLUDED.caller_channel_code,
    request_type = EXCLUDED.request_type,
    allowed_yn = EXCLUDED.allowed_yn,
    authentication_required_yn = EXCLUDED.authentication_required_yn,
    signature_required_yn = EXCLUDED.signature_required_yn,
    max_tps = EXCLUDED.max_tps,
    effective_from = EXCLUDED.effective_from,
    effective_to = EXCLUDED.effective_to,
    active_yn = EXCLUDED.active_yn,
    policy_version = EXCLUDED.policy_version,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP
