-- CMN-CALENDAR PostgreSQL vendor contract. Platform pack 미지원 상태에서는 fail-closed합니다.
CREATE TABLE IF NOT EXISTS cmn_business_calendar_day (
 calendar_id VARCHAR(50) NOT NULL,
 business_date DATE NOT NULL,
 business_day_yn CHAR(1) NOT NULL CHECK (business_day_yn IN ('Y','N')),
 day_type VARCHAR(30) NOT NULL DEFAULT 'BUSINESS',
 institution_code VARCHAR(50) NOT NULL DEFAULT '',
 reason VARCHAR(500) NOT NULL DEFAULT '',
 version_no BIGINT NOT NULL DEFAULT 1 CHECK (version_no > 0),
 created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
 created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
 updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (calendar_id,business_date)
);
