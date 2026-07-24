-- CMN 선택형 Sample Profile 초기 데이터입니다.
-- 운영 Product Seed가 아니며 CPF_CMN_SAMPLE_DB_ENABLED 같은 명시적 개발·검증
-- Profile에서만 실행합니다. 반복 실행 결과는 동일해야 합니다.

USE cmnDB;

INSERT INTO cmn_sample_item (
    sample_key,
    item_name,
    category_code,
    status_code,
    searchable_text,
    owner_reference,
    sort_order,
    version_no,
    created_by,
    updated_by
) VALUES
    ('CMN-SAMPLE-001', 'CPF CMN 기본 샘플', 'DATABASE', 'ACTIVE', 'connection migration crud search offset slice cursor', NULL, 10, 0, 'CMN_SAMPLE', 'CMN_SAMPLE'),
    ('CMN-SAMPLE-002', 'CPF CMN 비활성 샘플', 'VALIDATION', 'INACTIVE', 'validation duplicate optimistic-lock rollback', NULL, 20, 0, 'CMN_SAMPLE', 'CMN_SAMPLE')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    searchable_text = VALUES(searchable_text),
    owner_reference = VALUES(owner_reference),
    sort_order = VALUES(sort_order),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);
