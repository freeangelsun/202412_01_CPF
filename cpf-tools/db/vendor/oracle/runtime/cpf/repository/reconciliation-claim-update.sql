UPDATE CPF_UNKNOWN_RESULT
SET lease_owner = ?,
    lease_until = ?,
    attempt_count = attempt_count + 1,
    row_version = row_version + 1,
    updated_by = ?,
    updated_at = ?
WHERE unknown_id = ?
  AND row_version = ?
  AND unknown_status = 'CHECK_PENDING'
  AND (lease_until IS NULL OR lease_until < ?)
