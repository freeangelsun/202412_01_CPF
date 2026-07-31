DROP INDEX ix_bat_schedule_trigger_dispatch ON bat_schedule_trigger;
DROP INDEX uq_bat_schedule_trigger_idem ON bat_schedule_trigger;
ALTER TABLE bat_schedule_trigger DROP COLUMN updated_at, DROP COLUMN dispatched_at, DROP COLUMN last_error_at, DROP COLUMN last_error_code, DROP COLUMN attempt_count, DROP COLUMN dispatch_lease_until, DROP COLUMN dispatch_token, DROP COLUMN dispatch_owner, DROP COLUMN idempotency_key, DROP COLUMN fire_zone, DROP COLUMN business_date, DROP COLUMN definition_checksum, DROP COLUMN definition_version, DROP COLUMN job_id;
