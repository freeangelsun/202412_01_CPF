SELECT policy_key, standard_execution_id, original_channel_code, caller_channel_code,
       request_type, allowed_yn, authentication_required_yn, signature_required_yn,
       max_tps, effective_from, effective_to, active_yn, policy_version
FROM cpf_channel_execution_policy
ORDER BY policy_key
