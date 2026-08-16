INSERT INTO bat_schedule (
  schedule_id,job_id,definition_version,definition_checksum,cron_expression,calendar_id,
  business_day_only_yn,holiday_policy,available_start_time,available_end_time,
  run_date_pattern,timezone,enabled_yn,next_fire_at,created_by,created_at,updated_by,updated_at
)
VALUES (?,?,?,?,?,'DEFAULT',?,?,NULL,NULL,'yyyyMMdd',?,'Y',?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
