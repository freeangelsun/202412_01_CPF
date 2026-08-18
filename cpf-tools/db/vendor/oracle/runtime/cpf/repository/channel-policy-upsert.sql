MERGE INTO OPS_CHANNEL_EXECUTION_POLICY t
USING (SELECT ? policy_key, ? operation_id, ? caller_channel, ? allowed_yn, ? authentication_required_yn, ? signature_required_yn,
              ? max_tps, ? effective_from, ? effective_to, ? active_yn, ? policy_version, ? created_by, ? updated_by FROM dual) s
ON (t.policy_key = s.policy_key)
WHEN MATCHED THEN UPDATE SET t.operation_id=s.operation_id, t.caller_channel=s.caller_channel, t.allowed_yn=s.allowed_yn,
  t.authentication_required_yn=s.authentication_required_yn, t.signature_required_yn=s.signature_required_yn, t.max_tps=s.max_tps,
  t.effective_from=s.effective_from, t.effective_to=s.effective_to, t.active_yn=s.active_yn, t.policy_version=s.policy_version,
  t.updated_by=s.updated_by, t.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, operation_id, caller_channel, allowed_yn, authentication_required_yn, signature_required_yn,
  max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by)
VALUES (s.policy_key, s.operation_id, s.caller_channel, s.allowed_yn, s.authentication_required_yn, s.signature_required_yn,
  s.max_tps, s.effective_from, s.effective_to, s.active_yn, s.policy_version, s.created_by, s.updated_by)
