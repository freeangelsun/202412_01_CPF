UPDATE BAT_SCHEDULE
SET job_id = ?,
    definition_version = ?,
    definition_checksum = ?,
    cron_expression = ?,
    business_day_only_yn = ?,
    holiday_policy = ?,
    timezone = ?,
    enabled_yn = 'Y',
    next_fire_at = ?,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE schedule_id = ?
