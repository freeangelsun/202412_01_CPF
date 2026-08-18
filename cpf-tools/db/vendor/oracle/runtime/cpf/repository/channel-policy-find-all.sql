SELECT policy_key, operation_id, caller_channel,
       allowed_yn, authentication_required_yn, signature_required_yn,
       max_tps, effective_from, effective_to, active_yn, policy_version
FROM OPS_CHANNEL_EXECUTION_POLICY
ORDER BY policy_key
