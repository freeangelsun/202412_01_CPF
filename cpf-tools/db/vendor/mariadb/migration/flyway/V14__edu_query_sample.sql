USE cmnDB;

CREATE TABLE IF NOT EXISTS cmn_edu_query_item (
    item_id BIGINT NOT NULL COMMENT '교육 조회 항목 ID',
    item_name VARCHAR(200) NOT NULL COMMENT '교육 조회 항목명',
    category_code VARCHAR(30) NOT NULL COMMENT '교육 분류 코드',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 코드',
    owner_member_no VARCHAR(50) NULL COMMENT '예시 담당 회원 번호',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CMN' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (item_id),
    INDEX ix_cmn_edu_query_item_search (status_code, category_code, item_name),
    INDEX ix_cmn_edu_query_item_created (created_at, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN EDU 조회 샘플 항목';

INSERT INTO cmn_edu_query_item (
    item_id, item_name, category_code, status_code, owner_member_no, use_yn, created_by, updated_by
) VALUES
    (1, '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'M000000001', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'M000000002', 'Y', 'SYSTEM', 'SYSTEM'),
    (3, 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000003', 'Y', 'SYSTEM', 'SYSTEM'),
    (4, 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000004', 'Y', 'SYSTEM', 'SYSTEM'),
    (5, '검색 조건 정규화', 'QUERY', 'INACTIVE', 'M000000005', 'Y', 'SYSTEM', 'SYSTEM'),
    (6, '정렬 whitelist', 'QUERY', 'ACTIVE', 'M000000006', 'Y', 'SYSTEM', 'SYSTEM'),
    (7, '하위 호출 헤더 전파', 'HEADER', 'ACTIVE', 'M000000007', 'Y', 'SYSTEM', 'SYSTEM'),
    (8, 'Swagger 조회 예시', 'DOC', 'ACTIVE', 'M000000008', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    owner_member_no = VALUES(owner_member_no),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
