ALTER TABLE bat_schedule_trigger
  ADD COLUMN job_id VARCHAR(100), ADD COLUMN definition_version BIGINT, ADD COLUMN definition_checksum VARCHAR(128),
  ADD COLUMN business_date DATE, ADD COLUMN fire_zone VARCHAR(50), ADD COLUMN idempotency_key VARCHAR(200),
  ADD COLUMN dispatch_owner VARCHAR(160), ADD COLUMN dispatch_token BIGINT, ADD COLUMN dispatch_lease_until TIMESTAMP(6),
  ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0, ADD COLUMN last_error_code VARCHAR(100),
  ADD COLUMN last_error_at TIMESTAMP(6), ADD COLUMN dispatched_at TIMESTAMP(6),
  ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE UNIQUE INDEX uq_bat_schedule_trigger_idem ON bat_schedule_trigger(idempotency_key);
CREATE INDEX ix_bat_schedule_trigger_dispatch ON bat_schedule_trigger(trigger_status, dispatch_lease_until, scheduled_fire_at);
