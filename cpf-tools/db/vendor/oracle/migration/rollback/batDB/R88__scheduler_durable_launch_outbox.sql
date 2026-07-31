DROP INDEX ix_bat_schedule_trigger_dispatch;
DROP INDEX uq_bat_schedule_trigger_idem;
ALTER TABLE bat_schedule_trigger DROP (updated_at, dispatched_at, last_error_at, last_error_code, attempt_count, dispatch_lease_until, dispatch_token, dispatch_owner, idempotency_key, fire_zone, business_date, definition_checksum, definition_version, job_id);
