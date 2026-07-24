-- CMN 고객 업무 공통 Extension의 선택형 DB 검증 스키마입니다.
-- cpf-common은 기본적으로 DB 없이 동작하며, cmnDB에는 표준 DB 기능을 검증하는
-- cmn_sample_item 한 개만 둡니다. 업무 채번, 알림/업무 로그와 고정길이 전문
-- Runtime/Table은 CMN 기본 제품의 소유가 아닙니다.

USE cmnDB;

CREATE TABLE IF NOT EXISTS cmn_sample_item (
    sample_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '샘플 항목 ID',
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
    PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_cmn_sample_item_key UNIQUE (sample_key),
    CONSTRAINT ck_cmn_sample_item_status
        CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_cmn_sample_item_version
        CHECK (version_no >= 0),
    CONSTRAINT ck_cmn_sample_item_deleted
        CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_cmn_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_cmn_sample_item_name_sort (item_name, sample_item_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='CMN DB 연결·CRUD·검색·Paging·낙관적 잠금 검증용 단일 샘플';
