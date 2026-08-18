SELECT channel_code, channel_name, channel_type, trust_level,
       client_channel_yn, internal_channel_yn, authentication_required_yn,
       signature_required_yn, active_yn, description, policy_version
FROM OPS_CHANNEL_REGISTRY
ORDER BY channel_code
