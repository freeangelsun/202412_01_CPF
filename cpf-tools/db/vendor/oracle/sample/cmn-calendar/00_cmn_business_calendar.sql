-- CMN-CALENDAR Oracle vendor contract.
-- Oracle는 빈 문자열을 NULL로 취급하므로 선택 문자열 컬럼은 NULL 허용으로 정규화합니다.
-- Platform Oracle pack 자체가 미지원 상태라면 이 계약 파일만으로 지원 완료를 주장하지 않습니다.
BEGIN
  EXECUTE IMMEDIATE q'[
    CREATE TABLE cmn_business_calendar_day (
      calendar_id VARCHAR2(50) NOT NULL,
      business_date DATE NOT NULL,
      business_day_yn CHAR(1) NOT NULL,
      day_type VARCHAR2(30) DEFAULT 'BUSINESS' NOT NULL,
      institution_code VARCHAR2(50) NULL,
      reason VARCHAR2(500) NULL,
      version_no NUMBER(19) DEFAULT 1 NOT NULL,
      created_by VARCHAR2(100) DEFAULT 'SYSTEM' NOT NULL,
      created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
      updated_by VARCHAR2(100) DEFAULT 'SYSTEM' NOT NULL,
      updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
      CONSTRAINT pk_cmn_business_calendar_day PRIMARY KEY (calendar_id,business_date),
      CONSTRAINT ck_cmn_business_calendar_day_yn CHECK (business_day_yn IN ('Y','N')),
      CONSTRAINT ck_cmn_business_calendar_ver CHECK (version_no > 0)
    )
  ]';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -955 THEN RAISE; END IF;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE INDEX ix_cmn_business_calendar_date ON cmn_business_calendar_day (business_date,calendar_id)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
BEGIN
  EXECUTE IMMEDIATE 'CREATE INDEX ix_cmn_business_calendar_inst ON cmn_business_calendar_day (institution_code,business_date)';
EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;
/
