-- CPF generated SQL bundle: 00_optional_sample_seed.sql
-- 목적: 사용자가 선택한 CMN Sample/EDU 데이터만 반영
-- 정본은 specs/sql의 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File scripts/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- specs/sql/55_cmn_seed_data.sql
-- ============================================================================
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
