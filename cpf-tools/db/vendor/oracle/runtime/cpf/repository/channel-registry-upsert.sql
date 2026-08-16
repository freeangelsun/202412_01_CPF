MERGE INTO cpf_channel_registry target
USING (
    SELECT ? channel_code, ? channel_name, ? channel_type, ? trust_level,
           ? client_channel_yn, ? internal_channel_yn,
           ? authentication_required_yn, ? signature_required_yn,
           ? active_yn, ? description, ? policy_version, ? created_by, ? updated_by
    FROM dual
) source
ON (target.channel_code = source.channel_code)
WHEN MATCHED THEN UPDATE SET
    target.channel_name = source.channel_name,
    target.channel_type = source.channel_type,
    target.trust_level = source.trust_level,
    target.client_channel_yn = source.client_channel_yn,
    target.internal_channel_yn = source.internal_channel_yn,
    target.authentication_required_yn = source.authentication_required_yn,
    target.signature_required_yn = source.signature_required_yn,
    target.active_yn = source.active_yn,
    target.description = source.description,
    target.policy_version = source.policy_version,
    target.updated_by = source.updated_by,
    target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
    internal_channel_yn, authentication_required_yn, signature_required_yn,
    active_yn, description, policy_version, created_by, updated_by
) VALUES (
    source.channel_code, source.channel_name, source.channel_type, source.trust_level,
    source.client_channel_yn, source.internal_channel_yn, source.authentication_required_yn,
    source.signature_required_yn, source.active_yn, source.description,
    source.policy_version, source.created_by, source.updated_by
)
