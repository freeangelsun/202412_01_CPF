SELECT channel_code, channel_name, channel_type, trust_level,
       client_channel_yn, internal_channel_yn, authentication_required_yn,
       signature_required_yn, active_yn, description, policy_version
FROM cpf_channel_registry
ORDER BY channel_code
