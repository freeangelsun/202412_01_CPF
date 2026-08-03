ALTER TABLE bat_lock ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version';
ALTER TABLE bat_execution ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version';
ALTER TABLE bat_schedule ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version';
