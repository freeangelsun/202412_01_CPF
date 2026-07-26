SELECT schedule_id, job_id, next_fire_at, timezone
FROM bat_schedule
WHERE enabled_yn = 'Y'
  AND (next_fire_at IS NULL OR next_fire_at <= CURRENT_TIMESTAMP)
ORDER BY schedule_id
