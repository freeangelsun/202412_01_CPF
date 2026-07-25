-- CMN-CALENDAR MySQL vendor contract. Platform pack 미지원 상태에서는 이 DDL만으로 지원 완료를 주장하지 않습니다.
CREATE TABLE IF NOT EXISTS cmn_business_calendar_day (
 calendar_id VARCHAR(50) NOT NULL,
 business_date DATE NOT NULL,
 business_day_yn CHAR(1) NOT NULL,
 day_type VARCHAR(30) NOT NULL DEFAULT 'BUSINESS',
 institution_code VARCHAR(50) NOT NULL DEFAULT '',
 reason VARCHAR(500) NOT NULL DEFAULT '',
 version_no BIGINT NOT NULL DEFAULT 1,
 created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 PRIMARY KEY (calendar_id,business_date)
);
