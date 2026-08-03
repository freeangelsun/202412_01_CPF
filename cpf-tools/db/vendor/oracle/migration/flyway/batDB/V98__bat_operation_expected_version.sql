ALTER TABLE bat_lock ADD (row_version NUMBER(19) DEFAULT 0 NOT NULL);
ALTER TABLE bat_execution ADD (row_version NUMBER(19) DEFAULT 0 NOT NULL);
ALTER TABLE bat_schedule ADD (row_version NUMBER(19) DEFAULT 0 NOT NULL);
COMMENT ON COLUMN bat_lock.row_version IS '운영 위험조치 낙관적 잠금 Version';
COMMENT ON COLUMN bat_execution.row_version IS '운영 위험조치 낙관적 잠금 Version';
COMMENT ON COLUMN bat_schedule.row_version IS '운영 위험조치 낙관적 잠금 Version';
