ALTER TABLE bat_lock ADD COLUMN IF NOT EXISTS row_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE bat_execution ADD COLUMN IF NOT EXISTS row_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE bat_schedule ADD COLUMN IF NOT EXISTS row_version BIGINT NOT NULL DEFAULT 0;
COMMENT ON COLUMN bat_lock.row_version IS '운영 위험조치 낙관적 잠금 Version';
COMMENT ON COLUMN bat_execution.row_version IS '운영 위험조치 낙관적 잠금 Version';
COMMENT ON COLUMN bat_schedule.row_version IS '운영 위험조치 낙관적 잠금 Version';
