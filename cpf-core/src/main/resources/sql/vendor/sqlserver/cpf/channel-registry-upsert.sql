MERGE INTO cpf_channel_registry AS target
USING (VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)) AS source (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
    internal_channel_yn, authentication_required_yn, signature_required_yn,
    active_yn, description, policy_version, created_by, updated_by
)
ON target.channel_code = source.channel_code
WHEN MATCHED THEN UPDATE SET
    channel_name = source.channel_name,
    channel_type = source.channel_type,
    trust_level = source.trust_level,
    client_channel_yn = source.client_channel_yn,
    internal_channel_yn = source.internal_channel_yn,
    authentication_required_yn = source.authentication_required_yn,
    signature_required_yn = source.signature_required_yn,
    active_yn = source.active_yn,
    description = source.description,
    policy_version = source.policy_version,
    updated_by = source.updated_by,
    updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
    internal_channel_yn, authentication_required_yn, signature_required_yn,
    active_yn, description, policy_version, created_by, updated_by
) VALUES (
    source.channel_code, source.channel_name, source.channel_type, source.trust_level,
    source.client_channel_yn, source.internal_channel_yn, source.authentication_required_yn,
    source.signature_required_yn, source.active_yn, source.description,
    source.policy_version, source.created_by, source.updated_by
);
