INSERT INTO OPS_CHANNEL_EXECUTION_POLICY (
    policy_key, operation_id, caller_channel,
    allowed_yn, authentication_required_yn, signature_required_yn,
    max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    operation_id = VALUES(operation_id),
    caller_channel = VALUES(caller_channel),
    allowed_yn = VALUES(allowed_yn),
    authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn),
    max_tps = VALUES(max_tps),
    effective_from = VALUES(effective_from),
    effective_to = VALUES(effective_to),
    active_yn = VALUES(active_yn),
    policy_version = VALUES(policy_version),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP
