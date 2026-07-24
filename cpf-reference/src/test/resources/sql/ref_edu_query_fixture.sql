CREATE TABLE IF NOT EXISTS ref_sample_item (
    sample_item_id BIGINT NOT NULL AUTO_INCREMENT,
    sample_key VARCHAR(100) NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    category_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    status_code VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    searchable_text VARCHAR(500) NULL,
    owner_reference VARCHAR(100) NULL,
    sort_order BIGINT NOT NULL DEFAULT 0,
    version_no BIGINT NOT NULL DEFAULT 0,
    deleted_yn CHAR(1) NOT NULL DEFAULT 'N',
    transaction_global_id VARCHAR(34) NULL,
    idempotency_key VARCHAR(100) NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (sample_item_id),
    CONSTRAINT uk_ref_sample_item_key UNIQUE (sample_key),
    CONSTRAINT uk_ref_sample_item_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_ref_sample_item_status CHECK (status_code IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_ref_sample_item_version CHECK (version_no >= 0),
    CONSTRAINT ck_ref_sample_item_deleted CHECK (deleted_yn IN ('Y', 'N')),
    INDEX ix_ref_sample_item_status_sort (status_code, sort_order, sample_item_id),
    INDEX ix_ref_sample_item_category_sort (category_code, sort_order, sample_item_id),
    INDEX ix_ref_sample_item_name_sort (item_name, sample_item_id),
    INDEX ix_ref_sample_item_transaction (transaction_global_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'REF Minimal Transaction Reference Sample';

DELETE FROM ref_sample_item WHERE sample_item_id BETWEEN 90001 AND 90008;
DELETE FROM ref_sample_item WHERE sample_item_id BETWEEN 91000 AND 91999;

INSERT INTO ref_sample_item (
    sample_item_id,
    sample_key,
    item_name,
    category_code,
    status_code,
    searchable_text,
    owner_reference,
    sort_order,
    version_no,
    deleted_yn,
    created_by,
    created_at,
    updated_by,
    updated_at
) VALUES
    (90001, 'REF-MAPPER-90001', '단건 조회 샘플', 'SINGLE', 'ACTIVE', 'single', 'MBR-90001', 90001, 0, 'N', 'MAPPER_TEST', '2026-06-01 09:00:00.000', 'MAPPER_TEST', '2026-06-01 09:00:00.000'),
    (90002, 'REF-MAPPER-90002', '목록 조회 샘플', 'LIST', 'ACTIVE', 'list', 'MBR-90002', 90002, 0, 'N', 'MAPPER_TEST', '2026-06-02 09:00:00.000', 'MAPPER_TEST', '2026-06-02 09:00:00.000'),
    (90003, 'REF-MAPPER-90003', '검색 조회 샘플', 'SEARCH', 'ACTIVE', 'search', 'MBR-90003', 90003, 0, 'N', 'MAPPER_TEST', '2026-06-03 09:00:00.000', 'MAPPER_TEST', '2026-06-03 09:00:00.000'),
    (90004, 'REF-MAPPER-90004', '정렬 조회 샘플', 'SORT', 'ACTIVE', 'sort', 'MBR-90004', 90004, 0, 'N', 'MAPPER_TEST', '2026-06-04 09:00:00.000', 'MAPPER_TEST', '2026-06-04 09:00:00.000'),
    (90005, 'REF-MAPPER-90005', '페이지 조회 샘플', 'PAGE', 'ACTIVE', 'page', 'MBR-90005', 90005, 0, 'N', 'MAPPER_TEST', '2026-06-05 09:00:00.000', 'MAPPER_TEST', '2026-06-05 09:00:00.000'),
    (90006, 'REF-MAPPER-90006', '비활성 조회 샘플', 'LIST', 'INACTIVE', 'inactive', 'MBR-90006', 90006, 0, 'N', 'MAPPER_TEST', '2026-06-06 09:00:00.000', 'MAPPER_TEST', '2026-06-06 09:00:00.000'),
    (90007, 'REF-MAPPER-90007', 'Validation 조회 샘플', 'VALIDATION', 'INACTIVE', 'validation', 'MBR-90007', 90007, 0, 'N', 'MAPPER_TEST', '2026-06-07 09:00:00.000', 'MAPPER_TEST', '2026-06-07 09:00:00.000'),
    (90008, 'REF-MAPPER-90008', 'Keyset 조회 샘플', 'KEYSET', 'ACTIVE', 'keyset', 'MBR-90008', 90008, 0, 'N', 'MAPPER_TEST', '2026-06-08 09:00:00.000', 'MAPPER_TEST', '2026-06-08 09:00:00.000')
ON DUPLICATE KEY UPDATE
    sample_key = VALUES(sample_key),
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    searchable_text = VALUES(searchable_text),
    owner_reference = VALUES(owner_reference),
    sort_order = VALUES(sort_order),
    updated_by = 'MAPPER_TEST',
    updated_at = VALUES(updated_at);
