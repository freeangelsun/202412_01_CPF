UPDATE BAT_SCHEDULE
SET enabled_yn = ?,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP,
    row_version = row_version + 1
WHERE schedule_id = ?
  AND row_version = ?
