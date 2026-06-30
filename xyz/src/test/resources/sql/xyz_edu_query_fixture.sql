CREATE TABLE IF NOT EXISTS cmn_edu_query_item (
    item_id BIGINT NOT NULL COMMENT 'EDU 조회 샘플 항목 ID',
    item_name VARCHAR(100) NOT NULL COMMENT 'EDU 조회 샘플 항목명',
    category_code VARCHAR(30) NOT NULL COMMENT '조회 샘플 분류 코드',
    status_code VARCHAR(30) NOT NULL COMMENT '조회 샘플 상태 코드',
    owner_member_no VARCHAR(50) NULL COMMENT '샘플 소유 회원 번호',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록 일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 일시',
    PRIMARY KEY (item_id),
    INDEX ix_cmn_edu_query_item_search (status_code, category_code, item_name),
    INDEX ix_cmn_edu_query_item_created (created_at, item_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'EDU 조회 샘플 항목';

DELETE FROM cmn_edu_query_item WHERE item_id BETWEEN 90001 AND 90008;

INSERT INTO cmn_edu_query_item (
    item_id,
    item_name,
    category_code,
    status_code,
    owner_member_no,
    use_yn,
    created_by,
    created_at,
    updated_by,
    updated_at
) VALUES
    (90001, '단건 조회 샘플', 'SINGLE', 'ACTIVE', 'MBR-90001', 'Y', 'MAPPER_TEST', '2026-06-01 09:00:00.000000', 'MAPPER_TEST', '2026-06-01 09:00:00.000000'),
    (90002, '목록 조회 샘플', 'LIST', 'ACTIVE', 'MBR-90002', 'Y', 'MAPPER_TEST', '2026-06-02 09:00:00.000000', 'MAPPER_TEST', '2026-06-02 09:00:00.000000'),
    (90003, '검색 조회 샘플', 'SEARCH', 'ACTIVE', 'MBR-90003', 'Y', 'MAPPER_TEST', '2026-06-03 09:00:00.000000', 'MAPPER_TEST', '2026-06-03 09:00:00.000000'),
    (90004, '정렬 조회 샘플', 'SORT', 'ACTIVE', 'MBR-90004', 'Y', 'MAPPER_TEST', '2026-06-04 09:00:00.000000', 'MAPPER_TEST', '2026-06-04 09:00:00.000000'),
    (90005, '페이지 조회 샘플', 'PAGE', 'ACTIVE', 'MBR-90005', 'Y', 'MAPPER_TEST', '2026-06-05 09:00:00.000000', 'MAPPER_TEST', '2026-06-05 09:00:00.000000'),
    (90006, '비활성 조회 샘플', 'LIST', 'INACTIVE', 'MBR-90006', 'Y', 'MAPPER_TEST', '2026-06-06 09:00:00.000000', 'MAPPER_TEST', '2026-06-06 09:00:00.000000'),
    (90007, '미사용 제외 샘플', 'LIST', 'ACTIVE', 'MBR-90007', 'N', 'MAPPER_TEST', '2026-06-07 09:00:00.000000', 'MAPPER_TEST', '2026-06-07 09:00:00.000000'),
    (90008, 'Keyset 조회 샘플', 'KEYSET', 'ACTIVE', 'MBR-90008', 'Y', 'MAPPER_TEST', '2026-06-08 09:00:00.000000', 'MAPPER_TEST', '2026-06-08 09:00:00.000000')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    owner_member_no = VALUES(owner_member_no),
    use_yn = VALUES(use_yn),
    updated_by = 'MAPPER_TEST',
    updated_at = VALUES(updated_at);
