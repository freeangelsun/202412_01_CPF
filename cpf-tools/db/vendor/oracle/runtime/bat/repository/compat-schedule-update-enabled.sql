UPDATE bat_schedule
SET enabled_yn = ?,
    updated_by = ?,
    updated_at = SYSTIMESTAMP,
    row_version = row_version + 1
WHERE schedule_id = ?
  AND row_version = ?
