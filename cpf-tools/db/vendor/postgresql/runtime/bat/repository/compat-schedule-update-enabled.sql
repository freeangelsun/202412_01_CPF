UPDATE bat_schedule
SET enabled_yn = ?,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE schedule_id = ?
