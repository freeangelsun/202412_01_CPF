INSERT INTO cpf_channel_registry (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn,
    internal_channel_yn, authentication_required_yn, signature_required_yn,
    active_yn, description, policy_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    channel_name = VALUES(channel_name),
    channel_type = VALUES(channel_type),
    trust_level = VALUES(trust_level),
    client_channel_yn = VALUES(client_channel_yn),
    internal_channel_yn = VALUES(internal_channel_yn),
    authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn),
    active_yn = VALUES(active_yn),
    description = VALUES(description),
    policy_version = VALUES(policy_version),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP
