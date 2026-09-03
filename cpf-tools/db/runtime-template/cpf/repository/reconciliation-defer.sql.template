UPDATE CPF_UNKNOWN_RESULT
SET lease_owner = NULL,
    lease_until = NULL,
    next_check_at = ?,
    next_action = ?,
    row_version = row_version + 1,
    updated_by = ?,
    updated_at = ?
WHERE unknown_id = ?
  AND lease_owner = ?
  AND unknown_status = 'CHECK_PENDING'
