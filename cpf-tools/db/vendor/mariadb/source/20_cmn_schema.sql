-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=mariadb
-- schemaVersion=37
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=cmnDB
USE cmnDB;
CREATE TABLE IF NOT EXISTS cmn_business_calendar_day (
    calendar_id VARCHAR(50) NOT NULL COMMENT 'Calendar 식별자',
    business_date DATE NOT NULL COMMENT '기준 일자',
    business_day_yn CHAR(1) NOT NULL COMMENT '영업일 여부',
    day_type VARCHAR(30) NOT NULL DEFAULT 'BUSINESS' COMMENT 'BUSINESS/HOLIDAY/SPECIAL 등 일자 유형',
    institution_code VARCHAR(50) NULL COMMENT '선택 기관/시장 코드',
    reason VARCHAR(500) NULL COMMENT '휴일/예외 사유',
    version_no BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 시각',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정 시각',
    CONSTRAINT pk_cmn_business_calendar_day PRIMARY KEY (calendar_id, business_date),
    CONSTRAINT ck_cmn_business_calendar_day_yn CHECK (business_day_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_business_calendar_version CHECK (version_no > 0),
    INDEX ix_cmn_business_calendar_date (business_date, calendar_id),
    INDEX ix_cmn_business_calendar_institution (institution_code, business_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 영업일/휴일 Override 제품 정본';

CREATE TABLE IF NOT EXISTS cmn_sample_item (
    sample_item_id BIGINT AUTO_INCREMENT NOT NULL COMMENT '샘플 항목 ID',
    sample_key VARCHAR(100) NOT NULL COMMENT '외부 노출용 고유 샘플 키',
    item_name VARCHAR(200) NOT NULL COMMENT '샘플 항목명',
    category_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '검색 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    searchable_text VARCHAR(500) NULL COMMENT '검색 검증용 문자열',
    owner_reference VARCHAR(100) NULL COMMENT '다른 Domain을 직접 조인하지 않는 샘플 참조값',
    sort_order BIGINT NOT NULL DEFAULT 0 COMMENT '안정 정렬용 순번',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '논리 삭제 여부',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_cmn_sample_item PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_cmn_sample_item_key UNIQUE (sample_key),
    CONSTRAINT ck_cmn_sample_item_status CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_cmn_sample_item_version CHECK (version_no >= 0),
    CONSTRAINT ck_cmn_sample_item_deleted CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_cmn_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_name_sort (item_name, sample_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN DB 연결·CRUD·검색·Paging·낙관적 잠금 검증용 단일 샘플';
