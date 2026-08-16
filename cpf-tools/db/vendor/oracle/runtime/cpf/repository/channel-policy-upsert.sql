MERGE INTO cpf_channel_execution_policy target
USING (
    SELECT ? policy_key, ? standard_execution_id, ? original_channel_code,
           ? caller_channel_code, ? request_type, ? allowed_yn,
           ? authentication_required_yn, ? signature_required_yn, ? max_tps,
           ? effective_from, ? effective_to, ? active_yn, ? policy_version,
           ? created_by, ? updated_by
    FROM dual
) source
ON (target.policy_key = source.policy_key)
WHEN MATCHED THEN UPDATE SET
    target.standard_execution_id = source.standard_execution_id,
    target.original_channel_code = source.original_channel_code,
    target.caller_channel_code = source.caller_channel_code,
    target.request_type = source.request_type,
    target.allowed_yn = source.allowed_yn,
    target.authentication_required_yn = source.authentication_required_yn,
    target.signature_required_yn = source.signature_required_yn,
    target.max_tps = source.max_tps,
    target.effective_from = source.effective_from,
    target.effective_to = source.effective_to,
    target.active_yn = source.active_yn,
    target.policy_version = source.policy_version,
    target.updated_by = source.updated_by,
    target.updated_at = CURRENT_TIMESTAMP
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
)
