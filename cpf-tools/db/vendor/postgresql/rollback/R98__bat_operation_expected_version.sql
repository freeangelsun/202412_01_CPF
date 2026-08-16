ALTER TABLE bat_schedule DROP COLUMN IF EXISTS row_version;
ALTER TABLE bat_execution DROP COLUMN IF EXISTS row_version;
ALTER TABLE bat_lock DROP COLUMN IF EXISTS row_version;
