-- CMN-CALENDAR SQL Server vendor contract.
-- Platform SQL Server pack 자체가 미지원 상태라면 이 계약 파일만으로 지원 완료를 주장하지 않습니다.
IF OBJECT_ID('cmn_business_calendar_day','U') IS NULL
BEGIN
    CREATE TABLE cmn_business_calendar_day (
      calendar_id VARCHAR(50) NOT NULL,
      business_date DATE NOT NULL,
      business_day_yn CHAR(1) NOT NULL,
      day_type VARCHAR(30) NOT NULL CONSTRAINT df_cmn_calendar_day_type DEFAULT 'BUSINESS',
      institution_code VARCHAR(50) NOT NULL CONSTRAINT df_cmn_calendar_inst DEFAULT '',
      reason VARCHAR(500) NOT NULL CONSTRAINT df_cmn_calendar_reason DEFAULT '',
      version_no BIGINT NOT NULL CONSTRAINT df_cmn_calendar_ver DEFAULT 1,
      created_by VARCHAR(100) NOT NULL CONSTRAINT df_cmn_calendar_created_by DEFAULT 'SYSTEM',
      created_at DATETIME2(3) NOT NULL CONSTRAINT df_cmn_calendar_created_at DEFAULT SYSDATETIME(),
      updated_by VARCHAR(100) NOT NULL CONSTRAINT df_cmn_calendar_updated_by DEFAULT 'SYSTEM',
      updated_at DATETIME2(3) NOT NULL CONSTRAINT df_cmn_calendar_updated_at DEFAULT SYSDATETIME(),
      CONSTRAINT pk_cmn_business_calendar_day PRIMARY KEY (calendar_id,business_date),
      CONSTRAINT ck_cmn_business_calendar_day_yn CHECK (business_day_yn IN ('Y','N')),
      CONSTRAINT ck_cmn_business_calendar_ver CHECK (version_no > 0)
    );
    CREATE INDEX ix_cmn_business_calendar_date ON cmn_business_calendar_day (business_date,calendar_id);
    CREATE INDEX ix_cmn_business_calendar_inst ON cmn_business_calendar_day (institution_code,business_date);
END;
