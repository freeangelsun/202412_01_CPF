ALTER TABLE bat_schedule_trigger
  ADD COLUMN job_id VARCHAR(100) NULL,
  ADD COLUMN definition_version BIGINT NULL,
  ADD COLUMN definition_checksum VARCHAR(128) NULL,
  ADD COLUMN business_date DATE NULL,
  ADD COLUMN fire_zone VARCHAR(50) NULL,
  ADD COLUMN idempotency_key VARCHAR(200) NULL,
  ADD COLUMN dispatch_owner VARCHAR(160) NULL,
  ADD COLUMN dispatch_token BIGINT NULL,
  ADD COLUMN dispatch_lease_until DATETIME(6) NULL,
  ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
  ADD COLUMN last_error_code VARCHAR(100) NULL,
  ADD COLUMN last_error_at DATETIME(6) NULL,
  ADD COLUMN dispatched_at DATETIME(6) NULL,
  ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
CREATE UNIQUE INDEX uq_bat_schedule_trigger_idem ON bat_schedule_trigger(idempotency_key);
CREATE INDEX ix_bat_schedule_trigger_dispatch ON bat_schedule_trigger(trigger_status, dispatch_lease_until, scheduled_fire_at);
