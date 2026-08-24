UPDATE BAT_SCHEDULE
SET enabled_yn = 'N',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE schedule_id = ?
