UPDATE OPS_OPERATION_POLICY
SET enabled_yn='N', policy_version=policy_version+1, change_reason=?, updated_by=?, updated_at=CURRENT_TIMESTAMP(3)
WHERE operation_id=? AND policy_version=?
