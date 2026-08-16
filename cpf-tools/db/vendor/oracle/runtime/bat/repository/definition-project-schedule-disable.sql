UPDATE bat_schedule
SET enabled_yn = 'N',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE schedule_id = ?
