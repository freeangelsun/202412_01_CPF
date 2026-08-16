ALTER TABLE bat_schedule_trigger ADD (
  job_id VARCHAR2(100), definition_version NUMBER(19), definition_checksum VARCHAR2(128), business_date DATE,
  fire_zone VARCHAR2(50), idempotency_key VARCHAR2(200), dispatch_owner VARCHAR2(160), dispatch_token NUMBER(19),
  dispatch_lease_until TIMESTAMP(6), attempt_count NUMBER(10) DEFAULT 0 NOT NULL, last_error_code VARCHAR2(100),
  last_error_at TIMESTAMP(6), dispatched_at TIMESTAMP(6), updated_at TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX uq_bat_schedule_trigger_idem ON bat_schedule_trigger(idempotency_key);
CREATE INDEX ix_bat_schedule_trigger_dispatch ON bat_schedule_trigger(trigger_status, dispatch_lease_until, scheduled_fire_at);
