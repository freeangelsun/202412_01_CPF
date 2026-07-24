MERGE INTO cpf_channel_execution_policy AS target
USING (VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)) AS source (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code,
    request_type, allowed_yn, authentication_required_yn, signature_required_yn,
    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
)
ON target.policy_key = source.policy_key
WHEN MATCHED THEN UPDATE SET
    standard_execution_id = source.standard_execution_id,
    original_channel_code = source.original_channel_code,
    caller_channel_code = source.caller_channel_code,
    request_type = source.request_type,
    allowed_yn = source.allowed_yn,
    authentication_required_yn = source.authentication_required_yn,
    signature_required_yn = source.signature_required_yn,
    max_tps = source.max_tps,
    effective_from = source.effective_from,
    effective_to = source.effective_to,
    active_yn = source.active_yn,
    policy_version = source.policy_version,
    updated_by = source.updated_by,
    updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code,
    request_type, allowed_yn, authentication_required_yn, signature_required_yn,
    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES (
    source.policy_key, source.standard_execution_id, source.original_channel_code,
    source.caller_channel_code, source.request_type, source.allowed_yn,
    source.authentication_required_yn, source.signature_required_yn, source.max_tps,
    source.effective_from, source.effective_to, source.active_yn, source.policy_version,
    source.created_by, source.updated_by
);
