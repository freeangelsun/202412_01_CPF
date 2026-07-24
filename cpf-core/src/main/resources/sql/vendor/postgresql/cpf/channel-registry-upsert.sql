INSERT INTO cpf_channel_registry (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
    internal_channel_yn, authentication_required_yn, signature_required_yn,
    active_yn, description, policy_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (channel_code) DO UPDATE SET
    channel_name = EXCLUDED.channel_name,
    channel_type = EXCLUDED.channel_type,
    trust_level = EXCLUDED.trust_level,
    client_channel_yn = EXCLUDED.client_channel_yn,
    internal_channel_yn = EXCLUDED.internal_channel_yn,
    authentication_required_yn = EXCLUDED.authentication_required_yn,
    signature_required_yn = EXCLUDED.signature_required_yn,
    active_yn = EXCLUDED.active_yn,
    description = EXCLUDED.description,
    policy_version = EXCLUDED.policy_version,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP
